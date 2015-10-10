package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.player.Player;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class NaivePlayerTests {

    private NaivePlayer player;

    @Before
    public void setUp() {
        player = new NaivePlayer();
        player.setSide(Piece.CROSS);
    }

    @Test
    public void if_no_previous_sequence_then_pick_some_move() {
        GameState state = new GameState(5, new Piece[18][18], player.getSide());
        assertNotNull(player.move(state, null));
    }

    @Test
    public void it_continues_longest_sequence_when_possible() {
        // Row

        Piece[][] board = new Piece[18][18];
        board[2][2] = Piece.CROSS;
        board[2][3] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        // longest
        board[10][10] = Piece.CROSS;
        board[10][11] = Piece.CROSS;
        board[10][12] = Piece.CROSS;

        GameState state = new GameState(5, board, player.getSide());

        Cell move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 10);
        assertTrue(move.getColumn() == 9 || move.getColumn() == 13);


        // Column

        board = new Piece[18][18];
        board[2][2] = Piece.CROSS;
        board[2][3] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        // longest
        board[10][10] = Piece.CROSS;
        board[11][10] = Piece.CROSS;
        board[12][10] = Piece.CROSS;

        state = new GameState(5, board, player.getSide());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 || move.getRow() == 13);
        assertTrue(move.getColumn() == 10);

        // Diagonal left-right

        board = new Piece[18][18];
        board[2][2] = Piece.CROSS;
        board[2][3] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        // longest
        board[10][10] = Piece.CROSS;
        board[11][11] = Piece.CROSS;
        board[12][12] = Piece.CROSS;

        state = new GameState(5, board, player.getSide());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 && move.getColumn() == 9
                || move.getRow() == 13 && move.getColumn() == 13);

        // Diagonal right-left

        board = new Piece[18][18];
        board[2][2] = Piece.CROSS;
        board[2][3] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        // longest
        board[10][10] = Piece.CROSS;
        board[11][9] = Piece.CROSS;
        board[12][8] = Piece.CROSS;

        state = new GameState(5, board, player.getSide());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 && move.getColumn() == 11
                || move.getRow() == 13 && move.getColumn() == 7);
    }

    @Test
    public void when_continuing_longest_sequence_not_possible_then_make_as_long_as_possible() {
        Piece[][] board = new Piece[6][6];
        // Vertical (1,2) to (3,2) is longest, but blocked
        board[0][2] = Piece.ROUND;
        board[1][2] = Piece.CROSS;
        board[2][2] = Piece.CROSS;
        board[3][2] = Piece.CROSS;
        board[4][2] = Piece.ROUND;
        // Should continue (2,1), (2,2) with (2,3)
        board[2][1] = Piece.CROSS;
        // These are supposed to confuse sequence selection
        board[1][0] = Piece.ROUND;
        board[4][3] = Piece.ROUND;
        board[2][0] = Piece.ROUND;

        GameState startState = new GameState(5, board, Piece.CROSS);
        startState.print();

        Cell move = player.move(startState, null);

        GameState endState = startState.next(move);
        endState.print();
        assertEquals(2, move.getRow());
        assertEquals(3, move.getColumn());
    }

    @Test
    public void blocks_obvious_opponent_move() {
        Piece[][] board = new Piece[18][18];
        // Cross has 3 in sequence
        board[10][8] = Piece.CROSS;
        board[11][8] = Piece.CROSS;
        board[12][8] = Piece.CROSS;
        // ... but Round has 4 in sequence with one free end
        board[0][0] = Piece.ROUND;
        board[1][0] = Piece.ROUND;
        board[2][0] = Piece.ROUND;
        board[3][0] = Piece.ROUND;

        GameState startState = new GameState(5, board, Piece.CROSS);
        startState.print();

        Player player  = new NaivePlayer();
        player.setSide(Piece.CROSS);

        Cell move = player.move(startState, new Cell(3, 0));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(4, move.getRow());
        assertEquals(0, move.getColumn());
    }

    @Test
    public void takes_decisive_move_when_it_has_4_in_sequence() {
        Piece[][] board = new Piece[18][18];
        // Cross has 3 in sequence
        board[10][8] = Piece.CROSS;
        board[11][8] = Piece.CROSS;
        board[12][8] = Piece.CROSS;
        // ... and one other
        board[0][1] = Piece.CROSS;
        // ... but Round has 4 in sequence with one free end
        board[0][0] = Piece.ROUND;
        board[1][0] = Piece.ROUND;
        board[2][0] = Piece.ROUND;
        board[3][0] = Piece.ROUND;

        GameState startState = new GameState(5, board, Piece.ROUND);
        startState.print();

        Player player  = new NaivePlayer();
        player.setSide(Piece.ROUND);

        Cell move = player.move(startState, new Cell(0, 1));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(4, move.getRow());
        assertEquals(0, move.getColumn());
    }

    @Test
    public void takes_decisive_move_when_it_has_2_and_2_with_space_between() {
        Piece[][] board = new Piece[18][18];
        // Cross has 3 in sequence
        board[10][8] = Piece.CROSS;
        board[11][8] = Piece.CROSS;
        board[12][8] = Piece.CROSS;
        // ... and another 3
        board[0][1] = Piece.CROSS;
        board[0][2] = Piece.CROSS;
        board[0][3] = Piece.CROSS;
        // ... but Round will win by putting a piece
        board[0][0] = Piece.ROUND;
        board[1][0] = Piece.ROUND;
        board[2][0] = null; // ... here
        board[3][0] = Piece.ROUND;
        board[4][0] = Piece.ROUND;
        // ... instead of continuing the longest sequence which is this
        board[15][1] = Piece.ROUND;
        board[16][1] = Piece.ROUND;
        board[17][1] = Piece.ROUND;

        GameState startState = new GameState(5, board, Piece.ROUND);
        startState.print();

        Player player  = new NaivePlayer();
        player.setSide(Piece.ROUND);

        Cell move = player.move(startState, new Cell(0, 1));

        GameState endState = startState.next(move);
        endState.print();

        assertEquals(2, move.getRow());
        assertEquals(0, move.getColumn());
    }


    private void printMove(Cell move) {
        System.out.println(move.getRow() + ", " + move.getColumn());
    }
}
