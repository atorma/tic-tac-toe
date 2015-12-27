package org.atorma.tictactoe.game.state;

public class DenseMatrixBoard implements Board {
    private Piece[][] board;
    private int numPieces = 0;

    public DenseMatrixBoard(Piece[][] board) {
        this(board.length, board[0].length);
        for (int i = 0; i < getNumRows(); i++) {
            for (int j = 0; j < getNumCols(); j++) {
                if (board[i][j] != null) {
                    this.board[i][j] = board[i][j];
                    numPieces = numPieces + 1;
                }
            }
        }
    }

    public DenseMatrixBoard(int numRows, int numCols) {
        this.board = new Piece[numRows][numCols];
        this.numPieces = 0;
    }

    public int getNumRows() {
        return board.length;
    }

    public int getNumCols() {
        return board[0].length;
    }

    public int getNumPieces() {
        return numPieces;
    }

    public Piece get(Cell cell) {
        return board[cell.row][cell.column];
    }

    public void set(Cell cell, Piece piece) {
        if (piece != null && get(cell) == null) {
            numPieces = numPieces + 1;
        } else if (piece == null && get(cell) != null) {
            numPieces = numPieces - 1;
        }
        board[cell.row][cell.column] = piece;
    }

    public Board copy() {
        DenseMatrixBoard copy = new DenseMatrixBoard(getNumRows(), getNumCols());
        copy.numPieces = this.numPieces;
        copy.board = copy(this.board);
        return copy;
    }

    private Piece[][] copy(Piece[][] board) {
        Piece[][] copy = new Piece[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[0].length);
        }
        return copy;
    }
}
