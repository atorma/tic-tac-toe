package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.*;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monte Carlo Tree Search using a reinforcement learning
 * type of approach to score moves.
 *
 * References
 * * Browne et al, A Survey of Monte Carlo Tree Search Methods, IEEE Transactions on Computational Intelligence and AI in Games, vol. 4, no. 1, March 2012
 * * https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
 */
public class MCTSPlayer implements Player {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSPlayer.class);
    public static final MCTSParameters DEFAULT_PARAMS = new MCTSParameters();

    private MCTSParameters params;

    private Piece mySide;

    private int boardRowsNum, boardColsNum;
    private MoveNode lastMove; // Last move overall, may be my move or opponent's move, depending on algorithm progress
    private GameState lastState;

    private long planningStartTime;
    private AtomicInteger planningRollouts = new AtomicInteger();
    private ExecutorService workerPool;


    public MCTSPlayer() {
        this(DEFAULT_PARAMS);
    }

    public MCTSPlayer(MCTSParameters params) {
        Assert.isTrue(params.rolloutThreads >= 1);
        this.params = params;
        this.workerPool = Executors.newFixedThreadPool(params.rolloutThreads);
    }


    public String toString() {
        return "MCTS";
    }


    public void setPiece(Piece p) {
        this.mySide = p;
    }

    public Piece getPiece() {
        return this.mySide;
    }


    public MoveNode getLastMove() {
        return lastMove;
    }


    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (lastState == null || updatedState.getNumPieces() < lastState.getNumPieces()) {
            // Reset the learning following an earlier game,
            // otherwise we'll easily run out of memory in repeated games (tried that).
            boardRowsNum = updatedState.getBoardRows();
            boardColsNum = updatedState.getBoardCols();
            lastMove = new MoveNode(updatedState, opponentsLastMove, params.rewardScheme);
            lastState = updatedState.getCopy();
            LOGGER.debug("New game started!");
        } else {
            lastMove = lastMove.findMoveTo(opponentsLastMove);
            lastState = lastState.next(opponentsLastMove);
        }

        // Don't prune here. Seems it can cause so much GC activity that it steals CPU resources for simulation.

        /* Main work */
        lastMove = planMove();
        lastState = lastState.next(lastMove.getMove());

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
        GameState rolloutStartState;
        if (isMandatoryMove) { // If we have a mandatory move, use the time to plan ahead from that
            rolloutStartMove = bestMove;
            rolloutStartState = lastState.next(bestMove.getMove());
        } else {
            rolloutStartMove = lastMove;
            rolloutStartState = lastState;
        }
        List<Rectangle> searchRectangles = getSearchRectangles();


        planningRollouts.set(0);

        List<Future> results = new ArrayList<>();
        for (int i = 0; i < params.rolloutThreads; i++) {
            Runnable task = () -> {
                while (isThinkTimeLeft() && planningRollouts.get() < params.maxRolloutsNum) {
                    performRollout(rolloutStartState, rolloutStartMove, searchRectangles);
                }
            };
            results.add(workerPool.submit(task));
        }
        for (Future result : results) {
            try {
                result.get();
            } catch (InterruptedException e) {
                LOGGER.warn("Interrupted", e);
            } catch (ExecutionException e) {
                LOGGER.error("Rollout exception", e);
            }
        }



        if (!isMandatoryMove) {
            bestMove = selectNextMoveBasedOnExpectedReward();
        }

        LOGGER.debug(planningRollouts + " rollouts in " + (System.currentTimeMillis() - planningStartTime) + " ms");
        LOGGER.debug("Chose " + (isMandatoryMove ? "mandatory" : "MCTS") + " " + bestMove.printStatsFor(mySide));

        return bestMove;
    }


    private MoveNode checkForMandatoryMove() {
        // Check for a decisive move
        NaivePlayer myself = new NaivePlayer();
        myself.setPiece(mySide);
        Cell naiveMove = myself.move(lastState, null);
        GameState result = lastState.next(naiveMove);
        if (result.getWinner() == myself.getPiece()) {
            return lastMove.findMoveTo(naiveMove);
        }

        // If opponent would get a decisive move, steal the move
        GameState fakeState = GameState.builder()
                .setTemplate(lastState)
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
        List<Rectangle> searchAreas = new ArrayList<>();
        if (params.searchRadius < Integer.MAX_VALUE) {
            int numMoves = 0;
            MoveNode moveNode = lastMove;
            while (numMoves < params.pastMovesSearchNumber && moveNode != null && moveNode.getMove() != null) {
                Cell pos = moveNode.getMove();
                Rectangle rectangle = new Rectangle(
                        pos.getRow() - params.searchRadius, pos.getColumn() - params.searchRadius,
                        pos.getRow() + params.searchRadius, pos.getColumn() + params.searchRadius);
                searchAreas.add(rectangle);
                moveNode = moveNode.getParent();
                numMoves++;
            }
        }
        return searchAreas;
    }

    private void performRollout(GameState startState, MoveNode startNode, List<Rectangle> searchAreas) {

        // Selection and expansion
        LOGGER.trace("{} starts selecting node", Thread.currentThread());
        StateAndNode selected;
        synchronized (this) {
            selected = selectMove(startState, startNode, searchAreas);
            LOGGER.trace("{} selected node {}", Thread.currentThread(), startNode);
        }

        // Simulation
        LOGGER.trace("{} starts simulating game...", Thread.currentThread());
        GameState endState;
        if (params.simulationStrategy == MCTSParameters.SimulationStrategy.NAIVE) {
            NaivePlayer player1 = new NaivePlayer();
            player1.setPiece(mySide);
            NaivePlayer player2 = new NaivePlayer();
            player2.setPiece(mySide.other());
            Simulator simulator = new Simulator(selected.state, player1, player2);
            simulator.setCopyBoard(false); // NaivePlayer does not modify the input board when moving, this setting improves performance
            endState = simulateGame(simulator);
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.UNIFORM_RANDOM) {
            endState = simulateUniformRandomGame(selected.state);
        } else {
            throw new IllegalArgumentException("Invalid simulation strategy " + params.simulationStrategy);
        }
        LOGGER.trace("{} done simulating game", Thread.currentThread());

        // Back-propagation
        synchronized (this) {
            selected.node.propagateSimulatedResult(endState);
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
    private StateAndNode selectMove(GameState startState, MoveNode startNode, List<Rectangle> searchAreas) {
        MoveNode moveNode = startNode;
        GameState state = startState.getCopy();

        while (!state.isAtEnd()) {
            if (searchAreas.isEmpty()) {
                if (!moveNode.isFullyExpanded()) {
                    moveNode = moveNode.expandRandom();
                    state.update(moveNode.getMove());
                    return new StateAndNode(moveNode, state);
                } else {
                    moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                    state.update(moveNode.getMove());
                }
            } else {
                MoveNode child = moveNode.expandRandomIn(searchAreas);
                if (child != null) { // not yet fully expanded in searchAreas
                    moveNode = child;
                    state.update(moveNode.getMove());
                    return new StateAndNode(moveNode, state);
                } else { // all children visited at least once, now pick the currently most promising
                    if (moveNode.getChildren().size() > 0) {
                        moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                    } else { // search area is fully occupied, pick one outside it
                        moveNode = moveNode.expandRandom();
                    }
                    state.update(moveNode.getMove());
                }
            }

        }

        return new StateAndNode(moveNode, state);
    }


    /**
     * Simulates a game with uniformly random moves until it ends,
     * or time runs out, or maximum number of simulated turns is exceeded.
     * This method plays a fully random play, not using a Simulator for performance reasons.
     *
     * @return
     *  state where the game ended
     */
    private GameState simulateUniformRandomGame(GameState startingState) {
        GameState gameState = startingState.getCopy();
        int turns = 0;

        while (!gameState.isAtEnd()
                && (!params.maxThinkTimeIncludesSimulation || isThinkTimeLeft())
                && turns < params.maxSimulatedGameTurns) {

            List<Cell> allowedMoves = gameState.getAllowedMoves();
            Cell nextPosition = Utils.pickRandom(allowedMoves);
            gameState.update(nextPosition);
            turns++;
        }

        return gameState;
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
        Cell naiveMove = naivePlayer.move(lastState, lastMove.getMove());
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


    private static class StateAndNode {
        final MoveNode node;
        final GameState state;

        StateAndNode(MoveNode node, GameState state) {
            this.node = node;
            this.state = state;
        }
    }
}
