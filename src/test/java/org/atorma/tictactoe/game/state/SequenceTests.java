package org.atorma.tictactoe.game.state;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.UnitTests;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(FastTests.class)
public class SequenceTests extends UnitTests {

    @Test
    public void test_sequence_length() {
        Sequence sequence;

        sequence = new Sequence(new Cell(1, 2), new Cell(1, 3));
        assertEquals(2, sequence.getLength());

        sequence = new Sequence(new Cell(1, 2), new Cell(2, 2));
        assertEquals(2, sequence.getLength());

        sequence = new Sequence(new Cell(3, 3), new Cell(5, 5));
        assertEquals(3, sequence.getLength());

        sequence = new Sequence(new Cell(1, 3), new Cell(2, 2));
        assertEquals(2, sequence.getLength());

        sequence = new Sequence(new Cell(1, 3), new Cell(1, 3));
        assertEquals(1, sequence.getLength());

        sequence = new Sequence(null, null);
        assertEquals(0, sequence.getLength());
    }

    @Test
    public void test_sequence_direction() {
        Sequence sequence;

        sequence = new Sequence(new Cell(1, 2), new Cell(1, 3));
        assertTrue(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));

        sequence = new Sequence(new Cell(1, 2), new Cell(2, 2));
        assertFalse(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));

        sequence = new Sequence(new Cell(3, 3), new Cell(5, 5));
        assertFalse(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));

        sequence = new Sequence(new Cell(1, 3), new Cell(2, 2));
        assertFalse(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));

        sequence = new Sequence(new Cell(1, 3), new Cell(1, 3));
        assertTrue(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertTrue(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));

        sequence = new Sequence(null, null);
        assertFalse(sequence.hasDirection(Sequence.Direction.HORIZONTAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.VERTICAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL));
        assertFalse(sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL));
    }

}
