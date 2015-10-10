package org.atorma.tictactoe.game.state;

import java.util.Comparator;

/**
 * Defines Cell ordering that is ascending first by rows, then by columns.
 */
public class CellRowOrderComparator implements Comparator<Cell> {

    public int compare(Cell o1, Cell o2) {
        int rowDiff = o1.getRow() - o2.getRow();
        if (rowDiff != 0) {
            return rowDiff;
        } else {
            return o1.getColumn() - o2.getColumn();
        }
    }
}
