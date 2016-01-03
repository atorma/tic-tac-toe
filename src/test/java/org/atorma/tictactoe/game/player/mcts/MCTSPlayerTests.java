package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.SlowTests;
import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Category(SlowTests.class)
public class MCTSPlayerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSPlayerTests.class);

    @Test
    public void ties_or_wins_naive_player_in_3x3_tic_tac_toe_if_gets_to_start() {
        MCTSParameters params = new MCTSParameters();
        params.maxThinkTimeMillis = 1000;

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        GameState startState = GameState.builder()
                .setConnectHowMany(3)
                .setBoard(new Piece[3][3])
                .setNextPlayer(mctsPlayer.getPiece())
                .build();

        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);

        GameState endState = simulator.run();

        endState.print();
        assertTrue(mctsPlayer.getPiece() == endState.getWinner() || endState.isTie());
    }

    @Test
    public void ties_or_wins_naive_player_in_3x3_tic_tac_toe_if_does_not_get_to_start() {
        MCTSParameters params = new MCTSParameters();
        params.maxThinkTimeMillis = 1000;

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        // Naive player has started from the optimal position in the middle
        Piece[][] board = new Piece[3][3];
        board[1][1] = naivePlayer.getPiece();

        GameState startState = GameState.builder()
                .setConnectHowMany(3)
                .setBoard(board)
                .setNextPlayer(mctsPlayer.getPiece())
                .build();

        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);
        GameState endState = simulator.run();

        endState.print();
        assertTrue(mctsPlayer.getPiece() == endState.getWinner() || endState.isTie());
    }

    @Test
    public void beats_naive_player_in_18x18_connect_5_tic_tac_toe_even_when_naive_player_starts() {
        MCTSParameters params = new MCTSParameters();

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player naivePlayer = new NaivePlayer();
        naivePlayer.setPiece(Piece.O);

        GameState startState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[10][10])
                .setNextPlayer(naivePlayer.getPiece())
                .build();

        Simulator simulator = new Simulator(startState, mctsPlayer, naivePlayer);

        GameState endState = simulator.run();

        endState.print();
        assertEquals(mctsPlayer.getPiece(), endState.getWinner());
    }

    @Test
    public void beats_random_player_in_18x18_connect_5_game() {
        MCTSParameters params = new MCTSParameters();
        params.maxThinkTimeMillis = 1000;

        Player mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        Player randomPlayer = new RandomPlayer();
        randomPlayer.setPiece(Piece.O);

        GameState startState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[18][18])
                .setNextPlayer(randomPlayer.getPiece())
                .build();

        Simulator simulator = new Simulator(startState, mctsPlayer, randomPlayer);

        GameState endState = simulator.run();

        endState.print();
        assertEquals(mctsPlayer.getPiece(), endState.getWinner());
    }

    @Test
    public void mcts_player_chooses_decisive_move_in_one_rollout() {
        MCTSParameters params = new MCTSParameters();
        params.maxThinkTimeMillis = 1000;
        params.maxRolloutsNum = 1;
        params.searchRadius = Integer.MAX_VALUE; // give freedom to choose any free cell, but we still expect decisive move

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

        GameState startState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(mctsPlayer.getPiece())
                .build();
        startState.print();

        Cell mctsPlayerMove = mctsPlayer.move(startState, new Cell(3, 0));
        GameState endState = startState.next(mctsPlayerMove);
        endState.print();

        assertEquals(endState.getWinner(), mctsPlayer.getPiece());
    }

    @Test
    public void mcts_player_chooses_antidecisive_move_in_one_rollout() {
        MCTSParameters params = new MCTSParameters();
        params.maxThinkTimeMillis = 1000;
        params.maxRolloutsNum = 1;
        params.searchRadius = Integer.MAX_VALUE; // give freedom to choose any free cell, but we still expect decisive move

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


        GameState startState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(mctsPlayer.getPiece())
                .build();
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
        params.pruneParent = false;
        params.pruneSiblings = false;
        params.pruneDescendantLevelsGreaterThan = Integer.MAX_VALUE;

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


        GameState gameState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(mctsPlayer.getPiece())
                .build();
        gameState.print();

        Cell mctsPlayerMove = mctsPlayer.move(gameState, new Cell(8, 0));
        LOGGER.debug("Move alternatives were");
        for (MoveNode c : mctsPlayer.getLastMove().getParent().getChildren()) {
            LOGGER.debug("Move ({},{}), E[r] = {}, S = {}, X wins {}, O wins {}, Total {}",
                    c.getMove().getRow(), c.getMove().getColumn(),
                    c.getExpectedReward(mctsPlayer.getPiece()), c.getExplorationScore(mctsPlayer.getPiece()),
                    c.getWins(mctsPlayer.getPiece()), c.getWins(mctsPlayer.getPiece().other()), c.getNumPlays());
        }

        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        assertTrue(mctsPlayerMove.equals(new Cell(5, 0)) || mctsPlayerMove.equals(new Cell(9, 0)));

        Cell naivePlayerMove = naivePlayer.move(gameState, mctsPlayerMove);
        gameState = gameState.next(naivePlayerMove);
        gameState.print();

        mctsPlayerMove = mctsPlayer.move(gameState, naivePlayerMove);

        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        // Assuming that NaivePlayer has tried to elongate the sequence
        assertTrue(mctsPlayerMove.equals(new Cell(4, 0)) || mctsPlayerMove.equals(new Cell(10, 0)));
    }

    @Test
    public void test_when_opponent_has_already_made_first_move() {
        Player mctsPlayer = new MCTSPlayer();
        mctsPlayer.setPiece(Piece.X);

        Player opponent = new RandomPlayer();
        opponent.setPiece(Piece.O);

        Piece[][] board = new Piece[18][18];
        board[2][2] = opponent.getPiece();
        GameState state = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(mctsPlayer.getPiece()).build();

        Cell opponentsMove = new Cell(2, 2);

        Cell MCTSMove = mctsPlayer.move(state, opponentsMove);
        assertNotNull(MCTSMove);
    }

    @Test
    public void when_search_radius_given_then_search_expands_only_within_radius_around_specified_number_of_past_moves() {
        MCTSParameters params = new MCTSParameters();
        params.searchRadius = 1;
        params.pruneSiblings = false;
        params.pruneParent = false;
        params.pruneDescendantLevelsGreaterThan = Integer.MAX_VALUE;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        GameState gameState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[18][18])
                .setNextPlayer(mctsPlayer.getPiece())
                .build();
        gameState.print();

        // mctsPlayer starts from empty board, can expand anywhere
        Cell mctsPlayerMove = mctsPlayer.move(gameState, null);
        gameState = gameState.next(mctsPlayerMove);
        gameState.print();

        Cell opponentsMove = new Cell(0, 0);
        gameState = gameState.next(opponentsMove);
        gameState.print();

        // Both have moved, can now move only within params.searchRadius around the past moves
        Cell mctsPlayer2ndMove = mctsPlayer.move(gameState, opponentsMove);
        gameState = gameState.next(mctsPlayer2ndMove);
        gameState.print();

        assertTrue(Cell.getDistance(mctsPlayer2ndMove, mctsPlayerMove) <= params.searchRadius
                || Cell.getDistance(mctsPlayer2ndMove, opponentsMove) <= params.searchRadius);

        // Furthermore, it should expand (search) alternatives within search radius of past moves only
        MoveNode mctsPlayer2ndNode = mctsPlayer.getLastMove();
        assertTrue(mctsPlayer2ndNode.getChildren().size() > 0);
        for (MoveNode n : mctsPlayer2ndNode.getChildren()) {
            assertTrue(Cell.getDistance(n.getMove(), mctsPlayerMove) <= params.searchRadius
                    || Cell.getDistance(n.getMove(), opponentsMove) <= params.searchRadius);
        }
        MoveNode opponentNode = mctsPlayer2ndNode.getParent();
        assertTrue(opponentNode.getChildren().size() > 0);
        for (MoveNode n : opponentNode.getChildren()) {
            assertTrue(Cell.getDistance(n.getMove(), mctsPlayerMove) <= params.searchRadius
                    || Cell.getDistance(n.getMove(), opponentsMove) <= params.searchRadius);
        }
    }

    @Test
    public void when_opponent_has_started_then_search_expands_around_opponents_move() {
        MCTSParameters params = new MCTSParameters();
        params.maxRolloutsNum = 1;
        params.searchRadius = 1;
        params.pruneSiblings = false;
        params.pruneParent = false;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.X);

        GameState gameState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[18][18])
                .setNextPlayer(mctsPlayer.getPiece().other())
                .build();
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

    /**
     * This scenario is difficult for all MCTS simulation strategies.
     * Naive strategy fares best but even that requires a lot of rollouts.
     */
    @Test
    public void test_scenario_where_mcts_made_a_bad_move_in_game() {
        GameState state = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[18][18])
                .setNextPlayer(Piece.O)
                .build();

        state = state.next(new Cell(7, 13)); // 1
        state = state.next(new Cell(6, 12)); // 2
        state = state.next(new Cell(5, 13)); // 3
        state = state.next(new Cell(6, 13)); // 4
        state = state.next(new Cell(6, 14)); // 5
        state = state.next(new Cell(8, 12)); // 6
        state = state.next(new Cell(5, 12)); // 7
        state = state.next(new Cell(5, 14)); // 8
        state = state.next(new Cell(7, 12)); // 9
        state = state.next(new Cell(7, 11)); // 10
        state = state.next(new Cell(6, 11)); // 11
        state = state.next(new Cell(6, 10)); // 12
        state = state.next(new Cell(5, 9)); // 13
        state = state.next(new Cell(5, 11)); // 14
        state = state.next(new Cell(8, 13)); // 15
        state = state.next(new Cell(5, 10)); // 16
        state = state.next(new Cell(9, 14)); // 17
        state = state.next(new Cell(10, 15)); // 18
        state = state.next(new Cell(7, 10)); // 19
        state = state.next(new Cell(4, 13)); // 20
        state = state.next(new Cell(8, 9)); // 21
        state = state.next(new Cell(9, 8)); // 22
        state = state.next(new Cell(10, 14)); // 23
        state = state.next(new Cell(4, 12)); // 24
        state.print();

        MCTSParameters params = new MCTSParameters();
        params.simulationStrategy = MCTSParameters.SimulationStrategy.NAIVE;
        params.pruneParent = false;
        params.pruneSiblings = false;
        params.pruneDescendantLevelsGreaterThan = Integer.MAX_VALUE;
        // Usually passes when enough rollouts and memory
        params.maxThinkTimeMillis = Long.MAX_VALUE;
        params.maxRolloutsNum = 50000;

        MCTSPlayer mctsPlayer = new MCTSPlayer(params);
        mctsPlayer.setPiece(Piece.O);

        Cell mctsMove = mctsPlayer.move(state, new Cell(4, 12));
        state = state.next(mctsMove);
        state.print();
        LOGGER.debug("Alternatives:");
        List<MoveNode> alternatives = mctsPlayer.getLastMove().getParent().getChildren()
                .stream().sorted((n1, n2) -> (int) Math.signum(n1.getExpectedReward(Piece.O) - n2.getExpectedReward(Piece.O)))
                .collect(Collectors.toList());
        for (MoveNode moveNode : alternatives) {
            LOGGER.debug(moveNode.toString());
        }

        assertTrue(Arrays.asList(new Cell(7, 9), new Cell(3, 13)).contains(mctsMove));
    }
}
