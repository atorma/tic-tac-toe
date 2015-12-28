package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Configurable;
import org.atorma.tictactoe.game.player.Player;
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
    public static final MCTSParameters DEFAULT_PARAMS = new MCTSParameters();

    private MCTSParameters params;

    private Piece mySide;

    private int boardRowsNum, boardColsNum;
    private MoveNode lastMove; // Last move overall, may be my move or opponent's move, depending on algorithm progress

    private long planningStartTime;
    private AtomicInteger planningRollouts = new AtomicInteger();
    private ExecutorService workerPool;


    public MCTSPlayer() {
        this(DEFAULT_PARAMS);
    }

    public MCTSPlayer(MCTSParameters params) {
        if (params == null) throw new IllegalArgumentException("Parameters missing");

        this.params = params;
        this.workerPool =  Executors.newWorkStealingPool();
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
        if (lastMove == null || updatedState.getNumPieces() < lastMove.getGameState().getNumPieces()) {
            // Reset the learning following an earlier game,
            // otherwise we'll easily run out of memory in repeated games (tried that).
            boardRowsNum = updatedState.getBoardRows();
            boardColsNum = updatedState.getBoardCols();
            lastMove = new MoveNode(updatedState, opponentsLastMove, params.rewardScheme);
            LOGGER.debug("New game started! Simulation strategy {}.", params.simulationStrategy.toString().toLowerCase());
        } else {
            lastMove = lastMove.findMoveTo(opponentsLastMove);
        }

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
        planningStartTime = System.currentTimeMillis();

        MoveNode bestMove = checkForMandatoryMove();
        boolean isMandatoryMove = bestMove != null;


        MoveNode rolloutStartMove;
        if (isMandatoryMove) { // If we have a mandatory move, use the time to plan ahead from that
            rolloutStartMove = bestMove;
        } else {
            rolloutStartMove = lastMove;
        }
        List<Rectangle> searchRectangles = getSearchRectangles();

        planningRollouts.set(0);

        List<Future> results = new ArrayList<>();
        for (int i = 0; i < Runtime.getRuntime().availableProcessors() ; i++) {
            Runnable task = () -> {
                while (isThinkTimeLeft() && planningRollouts.get() < params.maxRolloutsNum) {
                    performRollout(rolloutStartMove, searchRectangles);
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


    private MoveNode checkForMandatoryMove() {
        // Check for a decisive move
        NaivePlayer myself = new NaivePlayer();
        myself.setPiece(mySide);
        Cell naiveMove = myself.move(lastMove.getGameState(), null);
        GameState result = lastMove.getGameState().next(naiveMove);
        if (result.getWinner() == myself.getPiece()) {
            return lastMove.findMoveTo(naiveMove);
        }

        // If opponent would get a decisive move, steal the move
        GameState fakeState = GameState.builder()
                .setTemplate(lastMove.getGameState())
                .setNextPlayer(mySide.other())
                .build();
        NaivePlayer opponent = new NaivePlayer();
        opponent.setPiece(mySide.other());
        naiveMove = opponent.move(fakeState, null);
        fakeState.update(naiveMove);
        if (fakeState.getWinner() == opponent.getPiece()) {
            return lastMove.findMoveTo(naiveMove);
        }

        return null;
    }


    private boolean isThinkTimeLeft() {
        return (System.currentTimeMillis() - planningStartTime) < params.maxThinkTimeMillis;
    }


    private List<Rectangle> getSearchRectangles() {
        if (params.searchRadius < Integer.MAX_VALUE) {
            GameState currentState = lastMove.getGameState();
            ArrayList<Rectangle> searchAreas = new ArrayList<>();
            for (int row = 0; row < boardRowsNum; row++) {
                for (int col = 0; col < boardColsNum; col++) {
                    if (currentState.getPiece(row, col) != null) {
                        Rectangle rectangle = new Rectangle(
                                row - params.searchRadius, col - params.searchRadius,
                                row + params.searchRadius, col + params.searchRadius);
                        searchAreas.add(rectangle);
                    }
                }
            }
            return searchAreas;
        } else {
            return null;
        }
    }

    private void performRollout(MoveNode startNode, List<Rectangle> searchAreas) {

        // Selection and expansion
        LOGGER.trace("{} starts selecting node", Thread.currentThread());
        MoveNode selected;
        synchronized (this) {
            selected = selectMove(startNode, searchAreas);
            LOGGER.trace("{} selected node {}", Thread.currentThread(), startNode);
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

        planningRollouts.incrementAndGet();
    }

    /**
     * Selection: each side chooses the best move given earlier simulations,
     * trying to maximize her own expected reward (+ exploration bonus).
     *
     * Returns a promising move, or one that ends the game.
     */
    private MoveNode selectMove(MoveNode startNode, List<Rectangle> searchAreas) {
        MoveNode moveNode = startNode;

        while (!moveNode.isEndState()) {
            if (searchAreas == null) {
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
            target = new Cell(boardRowsNum/2, boardColsNum/2);
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
