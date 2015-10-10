package org.atorma.tictactoe.game.state;

/**
 * Stores the game board as one array. This appears
 * to make its elements faster to access than with
 * a 2D array. The practical difference is very
 * roughly 30000 vs 28000 rollouts/5 sec.
 */
public class DenseArrayBoard implements Board {
    private final Piece[] board;
    private final int numRows;
    private final int numCols;

    public DenseArrayBoard(Piece[][] board) {
        this(board.length, board[0].length);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                set(i, j, board[i][j]);
            }
        }
    }

    public DenseArrayBoard(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.board = new Piece[numRows*numCols];
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public Piece get(int row, int col) {
        return board[getIndex(row, col)];
    }

    public void set(int row, int col, Piece piece) {
        board[getIndex(row, col)] = piece;
    }

    private int getIndex(int row, int col) {
        return row*numCols + col;
    }

    public Board copy() {
        DenseArrayBoard copy = new DenseArrayBoard(numRows, numCols);
        System.arraycopy(board, 0, copy.board, 0, board.length);
        return copy;
    }

}
