package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.*;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Monte Carlo Tree Search using a reinforcement learning
 * type of approach to score moves.
 *
 * References
 * * Browne et al, A Survey of Monte Carlo Tree Search Methods, IEEE Transactions on Computational Intelligence and AI in Games, vol. 4, no. 1, March 2012
 * * https://en.wikipedia.org/wiki/Monte_Carlo_tree_search
 */
public class MCTSPlayer implements Player {

    public static MCTSParameters DEFAULT_PARAMS = new MCTSParameters();

    private MCTSParameters params;

    private String name;
    private Piece mySide;

    private MoveNode lastMove; // Last move overall, may be my move or opponent's move, depending on algorithm progress

    private long planningStartTime;


    public MCTSPlayer(MCTSParameters params, String name) {
        this.name = name;
        this.params = params;
    }

    public MCTSPlayer(MCTSParameters params) {
        this(params, "MCTS");
    }

    public MCTSPlayer() {
        this(DEFAULT_PARAMS);
    }


    public String getName() {
        return this.name;
    }

    public String toString() {
        return getName();
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


    public Cell move(Piece[][] board, Cell opponentsMove) {
        GameState updatedState = new GameState(params.connectHowMany, board, this.getPiece());
        return move(updatedState, opponentsMove);
    }

    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (lastMove == null || updatedState.getNumPieces() < lastMove.getGameState().getNumPieces()) {
            // Reset the learning following an earlier game,
            // otherwise we'll easily run out of memory in repeated games (tried that).
            startNewGame(updatedState, opponentsLastMove);
        } else {
            lastMove = lastMove.findMoveTo(opponentsLastMove);
        }

        lastMove = planMove();

        if (params.pruneTreeAfterEachMove) {
            lastMove.pruneOtherBranchesOnPathToRoot(); // Prune moves that were never taken to mitigate memory issues
        }

        return lastMove.getMove();
    }

    private void startNewGame(GameState startingState, Cell opponentsMove) {
        params.boardRowsNum = startingState.getBoardRows();
        params.boardColsNum = startingState.getBoardCols();

        lastMove = new MoveNode(startingState, opponentsMove, params.rewardScheme);

        System.out.println("New game started!");
    }


    private MoveNode planMove() {
        planningStartTime = System.currentTimeMillis();

        MoveNode bestMove = checkForMandatoryMove();
        boolean isMandatoryMove = bestMove != null;

        int numIter = 0;
        while (isThinkTimeLeft() && numIter < params.maxRolloutsNum) {
            MoveNode rolloutStartMove = isMandatoryMove ? bestMove : lastMove; // If we have a mandatory move, use the time to plan ahead from that
            performRollout(rolloutStartMove, getSearchRectangles());
            numIter++;
        }

        if (!isMandatoryMove) {
            bestMove = selectNextMoveBasedOnExpectedReward(lastMove);
        }

        System.out.println(numIter + " rollouts in " + (System.currentTimeMillis() - planningStartTime) + " ms");
        System.out.println("Chose " + (isMandatoryMove ? "mandatory" : "MCTS") + " " + bestMove.printStatsFor(mySide));

        /*
        List<MoveNode> choices = new ArrayList<MoveNode>(bestMove.getParent().getChildren());
        Collections.sort(choices, new Comparator<MoveNode>() {
            public int compare(MoveNode o1, MoveNode o2) {
                return Double.valueOf(o2.getExpectedReward(mySide)).compareTo(Double.valueOf(o1.getExpectedReward(mySide)));
            }
        });
        for (MoveNode choice : choices) {
            System.out.println(choice.printStatsFor(mySide));
        }
        */

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
        GameState fakeState = new GameState(lastMove.getGameState(), mySide.other());
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

    private void performRollout(MoveNode startNode, List<Rectangle> searchAreas) {

        // Selection and expansion
        MoveNode selected = selectMove(startNode, searchAreas);

        // Simulation
        GameState endState;
        if (params.simulationStrategy == MCTSParameters.SimulationStrategy.NAIVE) {
            NaivePlayer player1 = new NaivePlayer();
            player1.setPiece(mySide);
            NaivePlayer player2 = new NaivePlayer();
            player2.setPiece(mySide.other());
            Simulator simulator = new Simulator(selected.getGameState(), player1, player2);
            simulator.setCopyBoard(false); // NaivePlayer does not modify the input board when moving, this setting improves performance
            endState = simulateGame(simulator);
        } else if (params.simulationStrategy == MCTSParameters.SimulationStrategy.UNIFORM_RANDOM) {
            endState = simulateUniformRandomGame(selected.getGameState());
        } else {
            throw new IllegalArgumentException("Invalid simulation strategy " + params.simulationStrategy);
        }

        // Back-propagation
        selected.propagateSimulatedResult(endState);
    }

    /**
     * Selection: each side chooses the best move given earlier simulations,
     * trying to maximize her own expected reward (+ exploration bonus).
     *
     * Returns a promising move, or one that ends the game.
     */
    private MoveNode selectMove(MoveNode startNode, List<Rectangle> searchAreas) {
        MoveNode moveNode = startNode;

        while (!moveNode.getGameState().isAtEnd()) {
            if (searchAreas.isEmpty()) {
                if (!moveNode.isFullyExpanded()) {
                    return moveNode.expandRandom();
                } else {
                    moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                }
            } else {
                MoveNode expanded = moveNode.expandRandomIn(searchAreas);
                if (expanded != null) { // not yet fully expanded in searchAreas
                    return expanded;
                } else {
                    if (moveNode.getChildren().size() > 0) {
                        moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                    } else { // can happen if search area is fully occupied
                        moveNode.expandRandom();
                        moveNode = Utils.pickRandom(moveNode.getBestExploratoryMoves());
                    }
                }
            }
        }

        return moveNode;
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


    private MoveNode selectNextMoveBasedOnExpectedReward(MoveNode opponentsLastMove) {
        // These have the highest expected reward for me
        List<MoveNode> candidates = opponentsLastMove.getBestMoves();

        // NaivePlayer always tries to elongate its longest sequence, so among equally well
        // rewarding moves, a naive move may be a good choice
        NaivePlayer naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(mySide);
        Cell naiveMove = naivePlayer.move(opponentsLastMove.getGameState(), opponentsLastMove.getMove());
        for (MoveNode candidate : candidates) {
            if (candidate.getMove().equals(naiveMove)) {
                return candidate;
            }
        }

        // Otherwise choose one that is closest to my previous move.
        // A center move is a good first move.
        final Cell target;
        if (opponentsLastMove.getParent() != null && opponentsLastMove.getParent().getMove() != null) {
            target = opponentsLastMove.getParent().getMove();
        } else {
            target = new Cell(params.boardRowsNum/2, params.boardColsNum/2);
        }
        candidates = Utils.max(candidates, element -> Cell.getDistance(element.getMove(), target));
        return Utils.pickRandom(candidates);
    }
}
