package org.atorma.tictactoe.game.state;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RectangleTests {

    @Test
    public void test_rectangle_contains_position() {
        Rectangle rectangle = new Rectangle(2,2, 4,7);

        assertTrue(rectangle.contains(new Cell(2, 2)));
        assertTrue(rectangle.contains(new Cell(4, 2)));
        assertTrue(rectangle.contains(new Cell(2, 7)));
        assertTrue(rectangle.contains(new Cell(4, 7)));
        assertTrue(rectangle.contains(new Cell(3, 5)));

        assertFalse(rectangle.contains(new Cell(1, 2)));
        assertFalse(rectangle.contains(new Cell(2, 1)));
        assertFalse(rectangle.contains(new Cell(5, 1)));
        assertFalse(rectangle.contains(new Cell(4, 1)));
        assertFalse(rectangle.contains(new Cell(1, 7)));
        assertFalse(rectangle.contains(new Cell(2, 8)));
        assertFalse(rectangle.contains(new Cell(4, 8)));
        assertFalse(rectangle.contains(new Cell(5, 7)));
    }

}
