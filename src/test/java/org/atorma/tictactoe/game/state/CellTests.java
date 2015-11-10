package org.atorma.tictactoe.game.state;

import org.atorma.tictactoe.FastTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(FastTests.class)
public class CellTests {

    @Test
    public void cells_are_equal_if_their_row_and_column_indices_are_the_same() {
        assertTrue(new Cell(1, 2).equals(new Cell(1, 2)));
        assertFalse(new Cell(1, 2).equals(new Cell(1, 3)));
        assertFalse(new Cell(1, 2).equals(new Cell(2, 1)));
        assertFalse(new Cell(1, 2).equals(null));
    }

    @Test
    public void distance_between_equal_cells_is_zero() {
        assertEquals(0, Cell.getDistance(new Cell(1, 2), new Cell(1, 2)));
    }

    @Test
    public void distance_when_cells_are_horizontally_or_vertically_aligned_is_horizontal_or_vertical_distance() {
        assertEquals(2, Cell.getDistance(new Cell(1, 2), new Cell(3, 2)));
        assertEquals(2, Cell.getDistance(new Cell(1, 2), new Cell(1, 4)));
    }

    @Test
    public void distance_when_cells_are_diagonally_aligned_is_diagonal_distance() {
        assertEquals(2, Cell.getDistance(new Cell(1, 2), new Cell(3, 4)));
        assertEquals(2, Cell.getDistance(new Cell(1, 2), new Cell(3, 0)));
    }

    @Test
    public void distance_when_cells_are_not_aligned_is_row_plus_column_distance() {
        assertEquals(3, Cell.getDistance(new Cell(1, 2), new Cell(0, 0)));
        assertEquals(3, Cell.getDistance(new Cell(1, 2), new Cell(2, 4)));
        assertEquals(5, Cell.getDistance(new Cell(1, 2), new Cell(4, 4)));
        assertEquals(5, Cell.getDistance(new Cell(1, 2), new Cell(4, 0)));
    }

}
