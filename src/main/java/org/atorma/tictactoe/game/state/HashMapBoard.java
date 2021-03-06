package org.atorma.tictactoe.game.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Not good: java.util.HashMap has a big memory print
 * and it's slow, too. Also better map implementations
 * (e.g. Int2ObjectOpenHashMap in FastUtil) are clearly
 * slower in getting elements than an array. Faster(?)
 * copy does not balance this loss.
 */
public class HashMapBoard implements Board {

    private final int numRows;
    private final int numCols;
    private final Map<Integer, Piece> map;

    public HashMapBoard(Piece[][] data) {
        this(data.length, data[0].length);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Cell cell = new Cell(i, j);
                set(cell, data[i][j]);
            }
        }
    }

    public HashMapBoard(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        map = new HashMap<>(numRows * numCols / 10);
    }


    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumPieces() {
        return map.size();
    }

    public Piece get(Cell cell) {
        return map.get(getIndex(cell.row, cell.column));
    }

    public void set(Cell cell, Piece piece) {
        if (piece != null) {
            map.put(getIndex(cell.row, cell.column), piece);
        } else {
            map.remove(getIndex(cell.row, cell.column));
        }
    }

    private int getIndex(int row, int col) {
        return row*numCols + col;
    }

    public Board copy() {
        HashMapBoard copy = new HashMapBoard(numRows, numCols);
        copy.map.putAll(this.map);
        return copy;
    }
}
