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
        player.setPiece(Piece.X);
    }

    @Test
    public void if_no_previous_sequence_then_pick_some_move() {
        GameState state = new GameState(5, new Piece[18][18], player.getPiece());
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

        GameState state = new GameState(5, board, player.getPiece());

        Cell move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 10);
        assertTrue(move.getColumn() == 9 || move.getColumn() == 13);


        // Column

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][10] = Piece.X;
        board[12][10] = Piece.X;

        state = new GameState(5, board, player.getPiece());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 || move.getRow() == 13);
        assertTrue(move.getColumn() == 10);

        // Diagonal left-right

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][11] = Piece.X;
        board[12][12] = Piece.X;

        state = new GameState(5, board, player.getPiece());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 && move.getColumn() == 9
                || move.getRow() == 13 && move.getColumn() == 13);

        // Diagonal right-left

        board = new Piece[18][18];
        board[2][2] = Piece.X;
        board[2][3] = Piece.X;
        board[3][2] = Piece.X;
        // longest
        board[10][10] = Piece.X;
        board[11][9] = Piece.X;
        board[12][8] = Piece.X;

        state = new GameState(5, board, player.getPiece());

        move = player.move(state, null);
        printMove(move);
        assertTrue(move.getRow() == 9 && move.getColumn() == 11
                || move.getRow() == 13 && move.getColumn() == 7);
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

        GameState startState = new GameState(5, board, Piece.X);
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
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... but Round has 4 in sequence with one free end
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        GameState startState = new GameState(5, board, Piece.X);
        startState.print();

        Player player  = new NaivePlayer();
        player.setPiece(Piece.X);

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
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... and one other
        board[0][1] = Piece.X;
        // ... but Round has 4 in sequence with one free end
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = Piece.O;
        board[3][0] = Piece.O;

        GameState startState = new GameState(5, board, Piece.O);
        startState.print();

        Player player  = new NaivePlayer();
        player.setPiece(Piece.O);

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
        board[10][8] = Piece.X;
        board[11][8] = Piece.X;
        board[12][8] = Piece.X;
        // ... and another 3
        board[0][1] = Piece.X;
        board[0][2] = Piece.X;
        board[0][3] = Piece.X;
        // ... but Round will win by putting a piece
        board[0][0] = Piece.O;
        board[1][0] = Piece.O;
        board[2][0] = null; // ... here
        board[3][0] = Piece.O;
        board[4][0] = Piece.O;
        // ... instead of continuing the longest sequence which is this
        board[15][1] = Piece.O;
        board[16][1] = Piece.O;
        board[17][1] = Piece.O;

        GameState startState = new GameState(5, board, Piece.O);
        startState.print();

        Player player  = new NaivePlayer();
        player.setPiece(Piece.O);

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
