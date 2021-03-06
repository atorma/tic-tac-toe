package org.atorma.tictactoe.game.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes one cell location on the board.
 * Position row = 0, column = 0 is the upper left
 * corner.
 */
public class Cell {

    public final int row;
    public final int column;

    @JsonCreator
    public Cell(@JsonProperty("row") int row, @JsonProperty("column") int column) {
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
        if (!(o instanceof Cell)) return false;

        Cell cell = (Cell) o;

        if (row != cell.row) return false;
        return column == cell.column;

    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + column;
        return result;
    }

    @Override
    public String toString() {
        return "Cell{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }
}
