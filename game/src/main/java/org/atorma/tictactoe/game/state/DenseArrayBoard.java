package org.atorma.tictactoe.game.state;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;

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
    private int numPieces;

    public DenseArrayBoard(Piece[][] board) {
        this(board.length, board[0].length);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Cell cell = new Cell(i, j);
                set(cell, board[i][j]);
            }
        }
    }

    public DenseArrayBoard(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.board = new Piece[numRows*numCols];
        this.numPieces = 0;
    }

    public int getNumRows() {
        return numRows;
    }

    public int getNumCols() {
        return numCols;
    }

    public int getNumPieces() {
        return numPieces;
    }

    public Piece get(Cell cell) {
        return board[getIndex(cell.row, cell.column)];
    }

    public void set(Cell cell, Piece piece) {
        int index = getIndex(cell.row, cell.column);
        if (piece != null && board[index] == null) {
            numPieces = numPieces + 1;
        } else if (piece == null && board[index] != null) {
            numPieces = numPieces - 1;
        }
        board[index] = piece;
    }

    private int getIndex(int row, int col) {
        return row*numCols + col;
    }

    public Board copy() {
        DenseArrayBoard copy = new DenseArrayBoard(numRows, numCols);
        System.arraycopy(board, 0, copy.board, 0, board.length);
        copy.numPieces = this.numPieces;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DenseArrayBoard that)) return false;
        return numRows == that.numRows && numCols == that.numCols && Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(board), numRows, numCols);
    }

    @JsonValue
    public DenseArrayBoardDTO toDTO() {
        var occupiedCells = new LinkedList<OccupiedCell>();
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                var piece = board[getIndex(row, col)];
                if (piece != null) {
                    occupiedCells.add(new OccupiedCell(piece, new Cell(row, col)));
                }
            }
        }
        return new DenseArrayBoardDTO(this.numRows, this.numCols, occupiedCells.toArray(new OccupiedCell[0]));
    }

    @JsonCreator
    public DenseArrayBoard(DenseArrayBoardDTO dto) {
        this(dto.numRows(), dto.numCols());
        for (OccupiedCell occupiedCell : dto.occupiedCells()) {
            this.set(occupiedCell.cell(), occupiedCell.piece());
        }
    }
}