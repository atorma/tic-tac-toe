package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.player.Player;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Category(FastTests.class)
public class NaivePlayerTests {

    private NaivePlayer player;

    @Before
    public void setUp() {
        player = new NaivePlayer();
        player.setPiece(Piece.X);
    }

    @Test
    public void if_no_previous_sequence_then_pick_some_move() {
        GameState state = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(new Piece[18][18])
                .setNextPlayer(player.getPiece())
                .build();
        assertNotNull(player.move(state, null));
    }

    @Test
    public void it_continues_longest_sequence_when_possible() {
        // Row

        Piece[][] board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[10][11] = Piece.X;
        board[10][12] = Piece.X;

        GameState state = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(player.getPiece())
                .build();

        Cell move = player.move(state, null);

        assertTrue(Arrays.asList(new Cell(10, 9), new Cell(10, 13)).contains(move));


        // Column

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][10] = Piece.X;
        board[12][10] = Piece.X;

        state = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(player.getPiece()).build();

        move = player.move(state, null);

        assertTrue(Arrays.asList(new Cell(9, 10), new Cell(13, 10)).contains(move));


        // Diagonal left-right

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][11] = Piece.X;
        board[12][12] = Piece.X;

        state = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(player.getPiece()).build();

        move = player.move(state, null);

        assertTrue(Arrays.asList(new Cell(9, 9), new Cell(13, 13)).contains(move));


        // Diagonal right-left

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][9] = Piece.X;
        board[12][8] = Piece.X;

        state = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(player.getPiece()).build();

        move = player.move(state, null);

        assertTrue(Arrays.asList(new Cell(9, 11), new Cell(13, 7)).contains(move));
    }

    @Test
    public void when_continuing_longest_sequence_not_possible_then_make_as_long_as_possible() {
        Piece[][] board = new Piece[6][6];
        // Vertical (1,2) to (3,2) is longest, but blocked
        board[0][2] = Piece.O;
        board[1][2] = Piece.X;
        board[2][2] = Piece.X;
        board[3][2] = Piece.X;
        board[4][2] = Piece.O;
        // Should continue (2,1), (2,2) with (2,3)
        board[2][1] = Piece.X;
        // These are supposed to confuse sequence selection
        board[1][0] = Piece.O;
        board[4][3] = Piece.O;
        board[2][0] = Piece.O;

        GameState startState = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(Piece.X).build();
        startState.print();

        Cell move = player.move(startState, null);

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(new Cell(2, 3), move);
    }

    @Test
    public void blocks_opponents_winning_move() {
        Piece[][] board = new Piece[18][18];
        // X has 3 in sequence
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... but O has 4 in sequence with one free end
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        GameState startState = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(Piece.X).build();
        startState.print();

        player.setPiece(Piece.X);
        Cell move = player.move(startState, new Cell(3, 0));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(new Cell(4, 0), move);
    }

    @Test
    public void takes_winning_move_when_it_has_4_in_sequence() {
        Piece[][] board = new Piece[18][18];
        // X has 3 in sequence
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... and one other
        board[0][1] = Piece.X;
        // ... but O has 4 in sequence with one free end
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        GameState startState = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(Piece.O)
                .build();
        startState.print();

        player.setPiece(Piece.O);
        Cell move = player.move(startState, new Cell(0, 1));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(new Cell(4, 0), move);
    }

    @Test
    public void takes_winning_move_when_it_has_2_and_2_with_space_between() {
        Piece[][] board = new Piece[18][18];
        // X has 3 in sequence
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... and another 3
        board[0][1] = Piece.X;
        board[0][2] = Piece.X;
        board[0][3] = Piece.X;
        // ... but O will win by putting a piece
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = null; // ... here
        board[3][0] = Piece.O;
        board[4][0] = Piece.O;
        // ... instead of continuing the longest sequence which is this
        board[15][1] = Piece.O;
        board[16][1] = Piece.O;
        board[17][1] = Piece.O;

        GameState startState = GameState.builder().setConnectHowMany(5).setBoard(board).setNextPlayer(Piece.O).build();
        startState.print();

        player.setPiece(Piece.O);
        Cell move = player.move(startState, new Cell(0, 1));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(new Cell(2, 0), move);
    }

    /**
     * X has 3 in left-right diagonal such that putting X in (3, 3), but not in (7, 7),
     * will yield a sequence of 4 with both ends free. O has no decisive move.
     * X is next so it should take the move that will yield 4 with both ends free.
     */
    @Test
    public void takes_move_that_will_yield_winning_move_in_players_next_turn() {
        Piece[][] board = new Piece[11][11];
        board[5][4] = Piece.X;
        board[5][5] = Piece.X;
        board[5][6] = Piece.X;
        board[4][4] = Piece.X;
        board[6][6] = Piece.X;
        board[5][8] = Piece.X;
        board[6][8] = Piece.O;
        board[7][8] = Piece.O;
        board[8][8] = Piece.O;
        board[6][7] = Piece.O;
        board[5][7] = Piece.O;
        board[4][5] = Piece.O;

        GameState state = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(Piece.X)
                .build();
        state.print();

        player.setPiece(Piece.X);
        Cell move = player.move(state, new Cell(5, 7));

        state.update(move);
        state.print();

        assertEquals(new Cell(3, 3), move);
    }

    /**
     * Same board as above, only now O is the next player. It should block X's decisive move.
     */
    @Test
    public void blocks_opponents_move_that_would_yield_winning_move_for_opponent_in_next_turn() {
        Piece[][] board = new Piece[11][11];
        board[5][4] = Piece.X;
        board[5][5] = Piece.X;
        board[5][6] = Piece.X;
        board[4][4] = Piece.X;
        board[6][6] = Piece.X;
        board[5][8] = Piece.X;
        board[6][8] = Piece.O;
        board[7][8] = Piece.O;
        board[8][8] = Piece.O;
        board[6][7] = Piece.O;
        board[5][7] = Piece.O;
        board[4][5] = Piece.O;

        GameState state = GameState.builder()
                .setConnectHowMany(5)
                .setBoard(board)
                .setNextPlayer(Piece.O)
                .build();
        state.print();

        player.setPiece(Piece.O);
        Cell move = player.move(state, new Cell(6, 6));

        state.update(move);
        state.print();

        assertEquals(new Cell(3, 3), move);
    }

}
