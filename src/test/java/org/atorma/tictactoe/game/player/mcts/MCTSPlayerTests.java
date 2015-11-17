package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.SlowTests;
import org.atorma.tictactoe.game.*;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(SlowTests.class)
public class MCTSPlayerTests {

    @Test
    public void ties_or_wins_naive_player_in_3x3_tic_tac_toe_if_gets_to_start() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 3;
        params.boardColsNum = 3;
        params.connectHowMany = 3;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxRolloutsNum = Integer.MAX_VALUE;
        params.searchRadius = Integer.MAX_VALUE;
        params.pastMovesSearchNumber = Integer.MAX_VALUE;
        params.rewardScheme = new WinLossDrawScheme();

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        GameState startState = new GameState.Builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(mctsPlayer.getPiece()).build();
        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);

        GameState endState = simulator.run();

        endState.print();
        assertTrue(mctsPlayer.getPiece() == endState.getWinner() || endState.isTie());
    }

    @Test
    public void ties_or_wins_naive_player_in_3x3_tic_tac_toe_if_does_not_get_to_start() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 3;
        params.boardColsNum = 3;
        params.connectHowMany = 3;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxRolloutsNum = Integer.MAX_VALUE;
        params.searchRadius = Integer.MAX_VALUE;
        params.pastMovesSearchNumber = Integer.MAX_VALUE;
        params.rewardScheme = new WinLossDrawScheme();

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        // Naive player has started from the optimal position in the middle
        Piece[][] board = new Piece[3][3];
        board[1][1] = naivePlayer.getPiece();
        GameState startState = new GameState.Builder().setConnectHowMany(3).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();

        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);
        GameState endState = simulator.run();

        endState.print();
        assertTrue(mctsPlayer.getPiece() == endState.getWinner() || endState.isTie());
    }

    @Test
    // Should pass _most_ of the time (due to randomness) - should be automated to assert win at frequency > threshold
    public void beats_naive_player_in_10x10_connect_5_tic_tac_toe_if_gets_to_start() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 10;
        params.boardColsNum = 10;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = Integer.MAX_VALUE;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 3000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 2;
        params.pastMovesSearchNumber = 4;
        params.rewardScheme = new WinLossDrawScheme();

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        GameState startState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(new Piece[params.boardRowsNum][params.boardColsNum]).setNextPlayer(mctsPlayer.getPiece()).build();
        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);

        GameState endState = simulator.run();

        endState.print();
        assertEquals(mctsPlayer.getPiece(), endState.getWinner());
    }

    @Test
    public void beats_random_player_in_18x18_connect_5_game() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = Integer.MAX_VALUE;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 2;
        params.pastMovesSearchNumber = 4;
        params.rewardScheme = new WinLossDrawScheme();

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player randomPlayer = new RandomPlayer();
        randomPlayer.setPiece(Piece.O);

        GameState startState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(new Piece[params.boardRowsNum][params.boardColsNum]).setNextPlayer(randomPlayer.getPiece()).build();
        Simulator simulator = new Simulator(startState, mctsPlayer, randomPlayer);

        GameState endState = simulator.run();

        endState.print();
        assertEquals(mctsPlayer.getPiece(), endState.getWinner());
    }

    @Test
    public void mcts_player_chooses_decisive_move_in_one_rollout() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = 1;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 2;
        params.pastMovesSearchNumber = 4;
        params.rewardScheme = new WinLossDrawScheme();

        Piece[][] board = new Piece[18][18];
        // Both have 4 in sequence
        board[10][8] = Piece.X;
        board[11][9] = Piece.X;
        board[12][10] = Piece.X;
        board[13][11] = Piece.X;
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        GameState startState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();
        startState.print();

        Cell mctsPlayerMove = mctsPlayer.move(startState, new Cell(3, 0));
        GameState endState = startState.next(mctsPlayerMove);
        endState.print();

        assertEquals(endState.getWinner(), mctsPlayer.getPiece());
    }

    @Test
    public void mcts_player_chooses_antidecisive_move_in_one_rollout() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = 1;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 2;
        params.pastMovesSearchNumber = 4;
        params.rewardScheme = new WinLossDrawScheme();

        Piece[][] board = new Piece[18][18];
        // Cross has 3 in sequence
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... but Round has 4 in sequence with one free end
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);


        GameState startState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();
        startState.print();

        Cell mctsPlayerMove = mctsPlayer.move(startState, new Cell(3, 0));
        GameState endState = startState.next(mctsPlayerMove);
        endState.print();

        assertEquals(4, mctsPlayerMove.getRow());
        assertEquals(0, mctsPlayerMove.getColumn());
    }

    @Test
    public void test_18x18_connect_5_mcts_player_blocks_3_in_row_with_free_ends_if_given_enough_time() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = Integer.MAX_VALUE;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 5000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 2;
        params.pastMovesSearchNumber = 4;
        params.rewardScheme = new WinLossDrawScheme();

        Piece[][] board = new Piece[18][18];
        // Cross has 2 in sequence
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... but Round has 3 in sequence with free ends.
        board[6][0] = Piece.O;
        board[7][0] = Piece.O;
        board[8][0] = Piece.O;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        NaivePlayer naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);


        GameState gameState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();
        gameState.print();

        Cell mctsPlayerMove = mctsPlayer.move(gameState, new Cell(8, 0));

        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        try {
            assertEquals(5, mctsPlayerMove.getRow());
            assertEquals(0, mctsPlayerMove.getColumn());
        } catch (AssertionError e) {
            assertEquals(9, mctsPlayerMove.getRow());
            assertEquals(0, mctsPlayerMove.getColumn());
        }

        Cell naivePlayerMove = naivePlayer.move(gameState, mctsPlayerMove);
        gameState = gameState.next(naivePlayerMove);
        gameState.print();

        mctsPlayerMove = mctsPlayer.move(gameState, naivePlayerMove);

        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        // Assuming that NaivePlayer has tried to elongate the sequence
        try {
            assertEquals(4, mctsPlayerMove.getRow());
            assertEquals(0, mctsPlayerMove.getColumn());
        } catch (AssertionError e) {
            assertEquals(10, mctsPlayerMove.getRow());
            assertEquals(0, mctsPlayerMove.getColumn());
        }
    }

    @Test
    public void test_when_opponent_has_already_made_first_move() {
        Player mctsPlayer = new MCTSPlayer();
        mctsPlayer.setPiece(Piece.X);

        Player opponent = new RandomPlayer();
        opponent.setPiece(Piece.O);

        Piece[][] board = new Piece[18][18];
        board[2][2] = opponent.getPiece();
        GameState state = new GameState.Builder().setConnectHowMany(5).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();

        Cell opponentsMove = new Cell(2, 2);

        Cell MCTSMove = mctsPlayer.move(state, opponentsMove);
        assertNotNull(MCTSMove);
    }

    @Test
    public void when_search_radius_given_then_search_expands_only_within_radius_around_specified_number_of_past_moves() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = 5;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 1;
        params.pastMovesSearchNumber = 2; // opponent's and own last moves
        params.rewardScheme = new WinLossDrawScheme();
        params.pruneTreeAfterEachMove = false;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        GameState gameState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(new Piece[params.boardRowsNum][params.boardColsNum]).setNextPlayer(mctsPlayer.getPiece()).build();
        gameState.print();

        // mctsPlayer starts from empty board, can expand anywhere
        Cell mctsPlayerMove = mctsPlayer.move(gameState, null);
        MoveNode mctsPlayerFirstNode = mctsPlayer.getLastMove();
        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        Cell opponentsMove = new Cell(0, 0);
        gameState = gameState.next(opponentsMove);
        gameState.print();

        // Both have moved, can within params.searchRadius around own or opponent's move
        Cell mctsPlayer2ndMove = mctsPlayer.move(gameState, opponentsMove);
        gameState = gameState.next(mctsPlayer2ndMove);
        gameState.print();

        // Check expanded children around the moves
        assertTrue(mctsPlayerFirstNode.getChildren().size() > 0);
        for (MoveNode child : mctsPlayerFirstNode.getChildren()) { // child is opponent's move
            assertTrue(Cell.getDistance(child.getMove(), mctsPlayerMove) <= params.searchRadius
                    || Cell.getDistance(child.getMove(), opponentsMove) <= params.searchRadius);
            for (MoveNode grandChild : child.getChildren()) { // grandChild is mctsPlayer's last move
                assertTrue(Cell.getDistance(grandChild.getMove(), mctsPlayerMove) <= params.searchRadius
                        || Cell.getDistance(grandChild.getMove(), opponentsMove) <= params.searchRadius);
            }
        }

        // Here we're assuming that the best move to play is chosen among expanded children of the current node.
        // Because only nodes around previous moves are expanded and there are no decisive moves, mctsPlayer's
        // move should be within the search area.
        assertTrue(Cell.getDistance(mctsPlayer2ndMove, mctsPlayerMove) <= params.searchRadius
        || Cell.getDistance(mctsPlayer2ndMove, opponentsMove) <= params.searchRadius);
    }

    @Test
    public void when_opponent_has_started_then_search_expands_around_opponents_move() {
        MCTSParameters params = new MCTSParameters();
        params.boardRowsNum = 18;
        params.boardColsNum = 18;
        params.connectHowMany = 5;
        params.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        params.maxRolloutsNum = 1;
        params.maxSimulatedGameTurns = Integer.MAX_VALUE;
        params.maxThinkTimeMillis = 1000;
        params.maxThinkTimeIncludesSimulation = true;
        params.searchRadius = 1;
        params.rewardScheme = new WinLossDrawScheme();
        params.pruneTreeAfterEachMove = false;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        GameState gameState = new GameState.Builder().setConnectHowMany(params.connectHowMany).setBoard(new Piece[params.boardRowsNum][params.boardColsNum]).setNextPlayer(mctsPlayer.getPiece().other()).build();
        gameState.print();

        Cell opponentsMove = new Cell(0, 0);
        gameState = gameState.next(opponentsMove);
        gameState.print();

        Cell mctsPlayerMove = mctsPlayer.move(gameState, opponentsMove);
        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        MoveNode opponentsMoveNode = mctsPlayer.getLastMove().getParent();
        assertTrue(opponentsMoveNode.getChildren().size() > 0);
        for (MoveNode child : opponentsMoveNode.getChildren()) {
            assertTrue(Cell.getDistance(child.getMove(), mctsPlayerMove) <= params.searchRadius
                    || Cell.getDistance(child.getMove(), opponentsMove) <= params.searchRadius);
        }

        assertTrue(Cell.getDistance(mctsPlayerMove, opponentsMove) <= params.searchRadius);
    }
}
