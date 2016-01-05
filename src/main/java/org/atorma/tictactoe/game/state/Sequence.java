package org.atorma.tictactoe.game.state;

public class Sequence {

    public enum Direction {
        HORIZONTAL, VERTICAL, LEFT_RIGHT_DIAGONAL, RIGHT_LEFT_DIAGONAL
    }

    private final Cell start;
    private final Cell end;
    private final int length;

    public Sequence(Cell start, Cell end) {
        if (start == null && end != null || start != null && end == null) throw new IllegalArgumentException();
        this.start = start;
        this.end = end;
        this.length = computeLength();
    }

    private int computeLength() {
        if (start == null || end == null) {
            return 0;
        } else {
            return Cell.getDistance(start, end) + 1;
        }
    }

    public int getLength() {
        return length;
    }

    public Cell getStart() {
        return start;
    }

    public Cell getEnd() {
        return end;
    }

    public boolean hasDirection(Direction direction) {
        if (start == null || end == null) {
            return false;
        }
        switch (direction) {
            case HORIZONTAL:
                return start.getRow() == end.getRow();
            case VERTICAL:
                return start.getColumn() == end.getColumn();
            case LEFT_RIGHT_DIAGONAL:
                return ( start.getRow() < end.getRow() && start.getColumn() < end.getColumn() ) || getLength() == 1;
            case RIGHT_LEFT_DIAGONAL:
                return ( start.getRow() < end.getRow() && start.getColumn() > end.getColumn() ) || getLength() == 1;
            default:
                return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sequence)) return false;

        Sequence sequence = (Sequence) o;

        if (start != null ? !start.equals(sequence.start) : sequence.start != null) return false;
        return end != null ? end.equals(sequence.end) : sequence.end == null;

    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (start == null) {
            return "Sequence{" +
                    "length=" + getLength() +
                    '}';
        } else {
            return "Sequence{" +
                    "length=" + getLength() +
                    ", start=(" + start.getRow() + ", " + start.getColumn() + ")" +
                    ", end=(" + end.getRow() + ", " + end.getColumn() + ")" +
                    '}';
        }
    }
}
