package org.atorma.tictactoe.game.state;

public class DenseMatrixBoard implements Board {
    private final Piece[][] board;

    public DenseMatrixBoard(Piece[][] board) {
        this.board = copy(board);
    }

    public int getNumRows() {
        return board.length;
    }

    public int getNumCols() {
        return board[0].length;
    }

    public Piece get(Cell cell) {
        return board[cell.row][cell.column];
    }

    public void set(Cell cell, Piece piece) {
        board[cell.row][cell.column] = piece;
    }

    public Board copy() {
        return new DenseArrayBoard(this.board);
    }

    private Piece[][] copy(Piece[][] board) {
        Piece[][] copy = new Piece[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[0].length);
        }
        return copy;
    }
}
