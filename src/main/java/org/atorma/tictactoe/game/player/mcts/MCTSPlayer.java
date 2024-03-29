package org.atorma.tictactoe.game.player.mcts;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Configurable;
import org.atorma.tictactoe.game.player.NearbyEmptyCellTracker;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.MandatoryMovePlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomNearbyPlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monte Carlo Tree Search using a reinforcement learning
 * type of approach to score moves.
 * <br>
 * References
 * * Browne et al, A Survey of Monte Carlo Tree Search Methods, IEEE Transactions on Computational Intelligence and AI in Games, vol. 4, no. 1, March 2012
 * * <a href="https://en.wikipedia.org/wiki/Monte_Carlo_tree_search">Wikipedia</a>
 */
public class MCTSPlayer implements Player, Configurable {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSPlayer.class);

    private static final MCTSParameters DEFAULT_PARAMS = new MCTSParameters();
    private MCTSParameters params;

    private Piece mySide;

    private GameState currentState;
    private Cell opponentsLastMove;
    // Stores MCTS results. Last move overall, may be my move or opponent's move, depending on algorithm progress.
    private MoveNode lastMove;

    private long planningStartTime;
    private final AtomicInteger planningRollouts = new AtomicInteger();
    private int numPlanningThreads;
    private ExecutorService workerPool;

    private MandatoryMovePlayer mandatoryMovePlayer;
    private NearbyEmptyCellTracker emptyCellTracker;

    public MCTSPlayer() {
        this(DEFAULT_PARAMS);
    }

    public MCTSPlayer(MCTSParameters params) {
        configure(params);
    }

    @JsonCreator
    public MCTSPlayer(MCTSPlayerDTO dto) {
        this(dto.params());
        this.mySide = dto.mySide();
        this.lastMove = dto.lastMove();
    }

    @JsonValue
    public MCTSPlayerDTO toDTO() {
        return new MCTSPlayerDTO(this.params, this.mySide, this.lastMove);
    }

    @Override
    public void configure(Object configuration) {
        MCTSParameters incoming = (MCTSParameters) configuration;
        if (incoming == null) throw new IllegalArgumentException("Parameters missing");
        if (incoming.numPlanningThreads != null && incoming.numPlanningThreads < 1)
            throw new IllegalArgumentException("Invalid planning thread number " + incoming.numPlanningThreads + ". Must be >= 1.");

        this.params = incoming;
        this.numPlanningThreads = Optional.ofNullable(incoming.numPlanningThreads)
                .orElse(Runtime.getRuntime().availableProcessors());
        this.workerPool = Executors.newFixedThreadPool(numPlanningThreads);

        this.mandatoryMovePlayer = new MandatoryMovePlayer(1) {
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

        LOGGER.info("Configuration: {}", params);
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
    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (lastMove == null) {
            lastMove = new MoveNode(updatedState, opponentsLastMove, params.rewardScheme);
            LOGGER.debug("New game started! Simulation strategy {}.", params.simulationStrategy.toString().toLowerCase());
        } else {
            lastMove = lastMove.findMoveTo(opponentsLastMove);
        }
        if (emptyCellTracker == null) {
            emptyCellTracker = new NearbyEmptyCellTracker(updatedState, params.searchRadius);
        }
        this.currentState = updatedState;
        this.opponentsLastMove = opponentsLastMove;

        // Don't prune here. Seems it can cause so much GC activity that it steals CPU resources for simulation.

        /* Main work */
        lastMove = planMove();

        LOGGER.debug("Best move selected out of {} expanded", lastMove.getParent().getChildren().size());

        if (params.pruneSiblings) {
            lastMove.pruneOtherBranchesOnPathToRoot();
        }
        if (params.pruneParent) {
            lastMove.makeRoot();
        }
        if (params.pruneDescendantLevelsGreaterThan < Integer.MAX_VALUE) {
            lastMove.pruneDescendantLevelsGreaterThan(params.pruneDescendantLevelsGreaterThan);
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MCTS tree size after pruning: {}", MoveNode.getTreeSize(lastMove.getRoot()));
        }

        return lastMove.getMove();
    }


    private MoveNode planMove() {
        planningStartTime = System.currentTimeMillis();

        emptyCellTracker.addOccupiedCell(currentState, opponentsLastMove);

        MoveNode bestMove = null;
        MoveNode rolloutStartMove;

        Cell mandatoryMove = mandatoryMovePlayer.move(currentState, opponentsLastMove);
        boolean isMandatoryMove = mandatoryMove != null;

        if (isMandatoryMove) {
            bestMove = lastMove.findMoveTo(mandatoryMove);
            // If we have a mandatory move, use the time to plan ahead from that state
            rolloutStartMove = bestMove;
            emptyCellTracker.addOccupiedCell(currentState, bestMove.getMove());
        } else {
            rolloutStartMove = lastMove;
        }

        planningRollouts.set(0);
        List<Future> results = new ArrayList<>();
        for (int i = 0; i < numPlanningThreads; i++) {
            Runnable task = () -> {
                while (isThinkTimeLeft() && planningRollouts.get() < params.maxRolloutsNum) {
                    planningRollouts.incrementAndGet();
                    runMctsIteration(rolloutStartMove);
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
            emptyCellTracker.addOccupiedCell(currentState, bestMove.getMove());
        }

        LOGGER.debug("{} rollouts in {} ms", planningRollouts, System.currentTimeMillis() - planningStartTime);
        LOGGER.debug("Chose {} {}", isMandatoryMove ? "mandatory" : "MCTS", bestMove.printStatsFor(mySide));

        return bestMove;
    }


    private boolean isThinkTimeLeft() {
        return (System.currentTimeMillis() - planningStartTime) < params.maxThinkTimeMillis;
    }

    private void runMctsIteration(MoveNode startNode) {
        // Selection and expansion
        MoveNode selected = selectMctsMoveThreadSafe(startNode);

        // Simulation and back-propagation
        LOGGER.trace("{} starts simulating games", Thread.currentThread());
        List<Player> players = initSimulationPlayers();
        for (int i = 0; i < params.gamesPerRollout; i++) {
            if (!isThinkTimeLeft()) {
                break;
            }
            GameState simulatedEndState = simulateGame(selected, players);
            propagateSimulationResult(selected, simulatedEndState);
        }
    }

    private MoveNode selectMctsMoveThreadSafe(MoveNode startNode) {
        LOGGER.trace("{} starts selecting node", Thread.currentThread());
        MoveNode selected;
        if (numPlanningThreads > 1) {
            synchronized (this) {
                selected = selectMctsMove(startNode);
            }
        } else {
            selected = selectMctsMove(startNode);
        }
        LOGGER.trace("{} selected node {}", Thread.currentThread(), selected);
        return selected;
    }

    /**
     * Selection: each side chooses the best move given earlier simulations,
     * trying to maximize her own expected reward (+ exploration bonus).
     * <p>
     * Returns a promising move, or one that ends the game.
     */
    private MoveNode selectMctsMove(MoveNode startNode) {
        MoveNode moveNode = startNode;

        while (!moveNode.isEndState()) {
            MoveNode child = moveNode.expandRandomIn(emptyCellTracker.getEmptyCellsNearOccupied());
            if (child != null) { // not yet fully expanded in nearby cells
                return child;
            } else { // all children visited at least once, now continue to searching in the most promising branch
                if (!moveNode.getChildren().isEmpty()) {
                    moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                } else { // search area is fully occupied, pick one outside it
                    moveNode = moveNode.expandRandom();
                }
            }
        }

        return moveNode;
    }

    private List<Player> initSimulationPlayers() {
        Player player1, player2;
        if (params.simulationStrategy == MCTSParameters.SimulationStrategy.NAIVE) {
            player1 = new NaivePlayer();
            player2 = new NaivePlayer();
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.RANDOM_NEARBY) {
            player1 = new RandomNearbyPlayer(params.searchRadius);
            player2 = new RandomNearbyPlayer(params.searchRadius);
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.UNIFORM_RANDOM) {
            player1 = new RandomPlayer();
            player2 = new RandomPlayer();
        } else {
            throw new IllegalArgumentException("Invalid simulation strategy " + params.simulationStrategy);
        }
        player1.setPiece(mySide);
        player2.setPiece(mySide.other());
        return Arrays.asList(player1, player2);
    }

    private GameState simulateGame(MoveNode start, List<Player> players) {
        LOGGER.trace("{} starts simulating game...", Thread.currentThread());
        long startTime = System.currentTimeMillis();

        Simulator simulator = new Simulator(start.getGameState(), players.get(0), players.get(1));
        simulator.setCopyBoard(false);

        long duration = System.currentTimeMillis() - startTime;
        LOGGER.trace("{} done simulating game in {} ms", Thread.currentThread(), duration);

        return runSimulator(simulator);
    }

    /**
     * Simulates a game until it ends, or time runs out, or maximum number of simulated turns is exceeded.
     * This method could be used to simulate a game that is played "well", not just fully at random.
     *
     * @return state where the game ended
     */
    private GameState runSimulator(Simulator simulator) {
        long maxSimulationTime;
        if (params.maxThinkTimeIncludesSimulation) {
            long elapsedTime = System.currentTimeMillis() - planningStartTime;
            maxSimulationTime = params.maxThinkTimeMillis - elapsedTime;
        } else {
            maxSimulationTime = Long.MAX_VALUE;
        }
        return simulator.run(maxSimulationTime, params.maxSimulatedGameTurns);
    }

    private void propagateSimulationResult(MoveNode simulationStartNode, GameState simulatedEndState) {
        long startTime = System.currentTimeMillis();

        if (numPlanningThreads > 1) {
            synchronized (this) {
                simulationStartNode.propagateSimulatedResult(simulatedEndState);
            }
        } else {
            simulationStartNode.propagateSimulatedResult(simulatedEndState);
        }


        long duration = System.currentTimeMillis() - startTime;
        LOGGER.trace("{} done propagating results of game in {} ms", Thread.currentThread(), duration);
    }


    private MoveNode selectNextMoveBasedOnExpectedReward() {
        // These have the highest expected reward for me
        List<MoveNode> candidates = lastMove.getBestMoves();

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

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
            target = new Cell(currentState.getBoardRows() / 2, currentState.getBoardCols() / 2);
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
