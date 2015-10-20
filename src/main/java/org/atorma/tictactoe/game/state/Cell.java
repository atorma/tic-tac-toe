package org.atorma.tictactoe.game.state;

/**
 * Describes one cell location on the board.
 * Position row = 0, column = 0 is the upper left
 * corner.
 */
public class Cell {

    public final int row;
    public final int column;

    public Cell(int row, int column) {
        this.row = row;
        this.column = column;
    }


    public int getColumn() {
        return this.column;
    }

    public int getRow() {
        return this.row;
    }


    public static int getDistance(Cell pos1, Cell pos2) {
        int hDist = Math.abs(pos1.getRow() - pos2.getRow());
        int vDist = Math.abs(pos1.getColumn() - pos2.getColumn());

        if (hDist == vDist) { // diagonally aligned
            return hDist;
        } else {
            return hDist + vDist;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Cell that = (Cell) o;

        if (row != that.row) return false;
        return column == that.column;

    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }
}
