package org.atorma.tictactoe.game.state;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("FastTests")
public class BigIntegerBoardTests {

    @Test
    public void it_gets_and_sets_piece_in_cell() {
        var board = new BigIntegerBoard(3, 3);
        assertNull(board.get(new Cell(1, 2)));
        assertEquals(0, board.getNumPieces());

        board.set(new Cell(1, 2), Piece.X);
        assertEquals(Piece.X, board.get(new Cell(1, 2)));
        assertEquals(1, board.getNumPieces());

        board.set(new Cell(0, 1), Piece.O);
        assertEquals(Piece.X, board.get(new Cell(1, 2)));
        assertEquals(Piece.O, board.get(new Cell(0, 1)));
        assertEquals(2, board.getNumPieces());

        board.set(new Cell(0, 1), null);
        assertEquals(Piece.X, board.get(new Cell(1, 2)));
        assertNull(board.get(new Cell(0, 1)));
        assertEquals(1, board.getNumPieces());
    }

    @Test
    public void it_constructs_board_from_piece_matrix_and_copies_it() {
        var board = new BigIntegerBoard(new Piece[][]{
                {Piece.X, Piece.O, Piece.X},
                {Piece.O, Piece.O, null},
                {null,    Piece.X, null},
        });
        assertEquals(6, board.getNumPieces());
        assertEquals(Piece.X, board.get(new Cell(0, 0)));

        var copy = board.copy();
        assertEquals(6, copy.getNumPieces());
        assertEquals(Piece.X, copy.get(new Cell(0, 0)));
        assertEquals(Piece.O, copy.get(new Cell(0, 1)));
        assertEquals(Piece.X, copy.get(new Cell(0, 2)));
        assertEquals(Piece.O, copy.get(new Cell(1, 0)));
        assertEquals(Piece.O, copy.get(new Cell(1, 1)));
        assertNull(copy.get(new Cell(1, 2)));
        assertNull(copy.get(new Cell(2, 0)));
        assertEquals(Piece.X, copy.get(new Cell(2, 1)));
        assertNull(copy.get(new Cell(2, 2)));
    }
}
