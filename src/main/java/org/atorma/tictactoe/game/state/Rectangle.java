package org.atorma.tictactoe.game.state;


public class Rectangle {

    private final int upperLeftRow;
    private final int upperLeftCol;
    private final int lowerRightRow;
    private final int lowerRightCol;

    public Rectangle(int upperLeftRow, int upperLeftCol, int lowerRightRow, int lowerRightCol) {
        this.upperLeftRow = upperLeftRow;
        this.upperLeftCol = upperLeftCol;
        this.lowerRightRow = lowerRightRow;
        this.lowerRightCol = lowerRightCol;
    }

    public Cell getUpperLeftCorner() {
        return new Cell(upperLeftRow, upperLeftCol);
    }

    public Cell getLowerRightCorner() {
        return new Cell(lowerRightRow, lowerRightCol);
    }

    public boolean contains(Cell position) {
        return position.getRow() >= upperLeftRow && position.getColumn() >= upperLeftCol &&
                position.getRow() <= lowerRightRow && position.getColumn() <= lowerRightCol;
    }

    @Override
    public String toString() {
        return "Rectangle{(" + upperLeftRow + ", " + upperLeftCol + "), " +
                "(" + lowerRightRow + ", " + lowerRightCol + ")}";
    }
}
