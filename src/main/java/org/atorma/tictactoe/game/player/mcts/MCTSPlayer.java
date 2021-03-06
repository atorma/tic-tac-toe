package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Configurable;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.MandatoryMovePlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomAdjacentPlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monte Carlo Tree Search using a reinforcement learning
 * type of approach to score moves.
 *
 * References
 * * Browne et al, A Survey of Monte Carlo Tree Search Methods, IEEE Transactions on Computational Intelligence and AI in Games, vol. 4, no. 1, March 2012
 * * https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
 */
public class MCTSPlayer implements Player, Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSPlayer.class);

    private static final MCTSParameters DEFAULT_PARAMS = new MCTSParameters();
    private MCTSParameters params;

    private Piece mySide;

    private GameState currentState;
    private Cell opponentsLastMove;
    private MoveNode lastMove; // Last move overall, may be my move or opponent's move, depending on algorithm progress
    private List<Rectangle> searchAreas = new ArrayList<>();

    private long planningStartTime;
    private AtomicInteger planningRollouts = new AtomicInteger();
    private ExecutorService workerPool;


    public MCTSPlayer() {
        this(DEFAULT_PARAMS);
    }

    public MCTSPlayer(MCTSParameters params) {
        if (params == null) throw new IllegalArgumentException("Parameters missing");
        if (params.numPlanningThreads < 1) throw new IllegalArgumentException("Invalid planning thread number " + params.numPlanningThreads + ". Must be >= 1.");

        this.params = params;
        this.workerPool =  Executors.newFixedThreadPool(params.numPlanningThreads);
    }


    @Override
    public Piece getPiece() {
        return this.mySide;
    }

    @Override
    public void setPiece(Piece p) {
        this.mySide = p;
    }

    @Override
    public void configure(Object configuration) {
        this.params = (MCTSParameters) configuration;
    }

    @Override
    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (currentState == null || updatedState.getNumPieces() <= currentState.getNumPieces()) {
            lastMove = new MoveNode(updatedState, opponentsLastMove, params.rewardScheme);
            LOGGER.debug("New game started! Simulation strategy {}.", params.simulationStrategy.toString().toLowerCase());
        } else {
            lastMove = lastMove.findMoveTo(opponentsLastMove);
        }
        this.currentState = updatedState;
        this.opponentsLastMove = opponentsLastMove;

        // Don't prune here. Seems it can cause so much GC activity that it steals CPU resources for simulation.

        /* Main work */
        lastMove = planMove();

        if (params.pruneSiblings) {
            lastMove.pruneOtherBranchesOnPathToRoot();
        }
        if (params.pruneParent) {
            lastMove.makeRoot();
        }
        if (params.pruneDescendantLevelsGreaterThan < Integer.MAX_VALUE) {
            lastMove.pruneDescendantLevelsGreaterThan(params.pruneDescendantLevelsGreaterThan);
        }

        return lastMove.getMove();
    }


    private MoveNode planMove() {
        MoveNode bestMove = null;

        planningStartTime = System.currentTimeMillis();

        MandatoryMovePlayer mandatoryMovePlayer = new MandatoryMovePlayer() {
            protected Cell planMove() {
                return getMandatoryMove().orElse(null);
            }

            @Override
            public void setPiece(Piece p) {
            }

            @Override
            public Piece getPiece() {
                return MCTSPlayer.this.getPiece();
            }
        };
        Cell mandatoryMove = mandatoryMovePlayer.move(currentState, opponentsLastMove);
        boolean isMandatoryMove = mandatoryMove != null;

        MoveNode rolloutStartMove;
        if (isMandatoryMove) { // If we have a mandatory move, use the time to plan ahead from that
            bestMove = lastMove.findMoveTo(mandatoryMove);
            rolloutStartMove = bestMove;
        } else {
            rolloutStartMove = lastMove;
        }

        updateSearchAreas();
        planningRollouts.set(0);
        List<Future> results = new ArrayList<>();
        for (int i = 0; i < params.numPlanningThreads ; i++) {
            Runnable task = () -> {
                while (isThinkTimeLeft() && planningRollouts.get() < params.maxRolloutsNum) {
                    performRollout(rolloutStartMove);
                }
            };
            results.add(workerPool.submit(task));
        }
        LOGGER.trace("Created {} planning tasks", results.size());
        for (Future result : results) {
            try {
                result.get();
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted", e);
            } catch (ExecutionException e) {
                LOGGER.error("Rollout exception", e);
                throw new RuntimeException(e);
            }
        }

        if (!isMandatoryMove) {
            bestMove = selectNextMoveBasedOnExpectedReward();
        }

        LOGGER.debug("{} rollouts in {} ms", planningRollouts, System.currentTimeMillis() - planningStartTime);
        LOGGER.debug("Chose {} {}", isMandatoryMove ? "mandatory" : "MCTS", bestMove.printStatsFor(mySide));

        return bestMove;
    }


    private boolean isThinkTimeLeft() {
        return (System.currentTimeMillis() - planningStartTime) < params.maxThinkTimeMillis;
    }


    private void updateSearchAreas() {
        searchAreas.clear();
        if (params.searchRadius < Integer.MAX_VALUE) {
            for (int row = 0; row < currentState.getBoardRows(); row++) {
                for (int col = 0; col < currentState.getBoardCols(); col++) {
                    if (currentState.getPiece(row, col) != null) {
                        Rectangle rectangle = new Rectangle(
                                row - params.searchRadius, col - params.searchRadius,
                                row + params.searchRadius, col + params.searchRadius);
                        searchAreas.add(rectangle);
                    }
                }
            }
        }
    }

    private void performRollout(MoveNode startNode) {
        planningRollouts.incrementAndGet();

        // Selection and expansion
        LOGGER.trace("{} starts selecting node", Thread.currentThread());
        MoveNode selected;
        synchronized (this) {
            selected = selectMctsMove(startNode);
            LOGGER.trace("{} selected node {}", Thread.currentThread(), selected);
        }

        // Simulation
        LOGGER.trace("{} starts simulating game...", Thread.currentThread());
        Player player1, player2;
        GameState endState;
        if (params.simulationStrategy == MCTSParameters.SimulationStrategy.NAIVE) {
            player1 = new NaivePlayer();
            player2 = new NaivePlayer();
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.RANDOM_ADJACENT) {
            player1 = new RandomAdjacentPlayer();
            player2 = new RandomAdjacentPlayer();
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.UNIFORM_RANDOM) {
            player1 = new RandomPlayer();
            player2 = new RandomPlayer();
        } else {
            throw new IllegalArgumentException("Invalid simulation strategy " + params.simulationStrategy);
        }
        player1.setPiece(mySide);
        player2.setPiece(mySide.other());
        Simulator simulator = new Simulator(selected.getGameState(), player1, player2);
        simulator.setCopyBoard(false);
        endState = simulateGame(simulator);
        LOGGER.trace("{} done simulating game", Thread.currentThread());

        // Back-propagation
        synchronized (this) {
            selected.propagateSimulatedResult(endState);
            LOGGER.trace("{} done propagating results", Thread.currentThread());
        }
    }

    /**
     * Selection: each side chooses the best move given earlier simulations,
     * trying to maximize her own expected reward (+ exploration bonus).
     *
     * Returns a promising move, or one that ends the game.
     */
    private MoveNode selectMctsMove(MoveNode startNode) {
        MoveNode moveNode = startNode;

        while (!moveNode.isEndState()) {
            if (searchAreas.isEmpty()) {
                if (!moveNode.isFullyExpanded()) {
                    return moveNode.expandRandom();
                } else {
                    moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                }
            } else {
                MoveNode child = moveNode.expandRandomIn(searchAreas);
                if (child != null) { // not yet fully expanded in searchAreas
                    return child;
                } else { // all children visited at least once, now continue to searching in the most promising branch
                    if (moveNode.getChildren().size() > 0) {
                        moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                    } else { // search area is fully occupied, pick one outside it
                        moveNode = moveNode.expandRandom();
                    }
                }
            }
        }

        return moveNode;
    }

    /**
     * Simulates a game until it ends, or time runs out, or maximum number of simulated turns is exceeded.
     * This method could be used to simulate a game that is played "well", not just fully at random.
     *
     * @return
     *  state where the game ended
     */
    private GameState simulateGame(Simulator simulator) {
        long maxSimulationTime;
        if (params.maxThinkTimeIncludesSimulation) {
            long elapsedTime = System.currentTimeMillis() - planningStartTime;
            maxSimulationTime =  params.maxThinkTimeMillis - elapsedTime;
        } else {
            maxSimulationTime = Long.MAX_VALUE;
        }
        return simulator.run(maxSimulationTime, params.maxSimulatedGameTurns);
    }


    private MoveNode selectNextMoveBasedOnExpectedReward() {
        // These have the highest expected reward for me
        List<MoveNode> candidates = lastMove.getBestMoves();

        // NaivePlayer always tries to elongate its longest sequence, so among equally well
        // rewarding moves, a naive move may be a good choice
        NaivePlayer naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(mySide);
        Cell naiveMove = naivePlayer.move(lastMove.getGameState(), lastMove.getMove());
        for (MoveNode candidate : candidates) {
            if (candidate.getMove().equals(naiveMove)) {
                return candidate;
            }
        }

        // Otherwise choose one that is closest to my previous move.
        // A center move is a good first move.
        final Cell target;
        if (lastMove.getParent() != null && lastMove.getParent().getMove() != null) {
            target = lastMove.getParent().getMove();
        } else {
            target = new Cell(currentState.getBoardRows()/2, currentState.getBoardCols()/2);
        }
        candidates = Utils.max(candidates, element -> Cell.getDistance(element.getMove(), target));
        return Utils.pickRandom(candidates);
    }

    public MoveNode getLastMove() {
        return lastMove;
    }

    @Override
    public String toString() {
        return "MCTS (" + params.simulationStrategy.toString().toLowerCase() + ")";
    }

}
