package org.atorma.tictactoe.game.state;

import java.math.BigInteger;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Adapted from
 * <a href="https://github.com/jjak0b/MNK-game/blob/master/src/player/HashableBoardState.java">"https://github.com/jjak0b/MNK-game/blob/master/src/player/HashableBoardState.java</a>
 * <p>
 * Too slow compared to DenseArrayBoard.
 */
public class BigIntegerBoard implements Board {
    private static final Map<Piece, BigInteger> PLAYER_BITS;
    static {
        PLAYER_BITS = new EnumMap<>(Piece.class);
        PLAYER_BITS.put(Piece.X, BigInteger.ONE); // 01
        PLAYER_BITS.put(Piece.O, BigInteger.TWO); // 10
    }
    private static final BigInteger MASK_BITS = new BigInteger("3"); // 11

    private BigInteger boardState;
    private final int numRows;
    private final int numCols;
    private int numPieces;

    public BigIntegerBoard(int numRows, int numCols) {
        this.numRows = numRows;
        this.numCols = numCols;
        this.boardState = BigInteger.ZERO;
    }

    public BigIntegerBoard(Piece[][] board) {
        this(board.length, board[0].length);
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Cell cell = new Cell(i, j);
                set(cell, board[i][j]);
            }
        }
    }

    @Override
    public int getNumRows() {
        return numRows;
    }

    @Override
    public int getNumCols() {
        return numCols;
    }

    @Override
    public Piece get(Cell cell) {
        var bits = boardState.shiftRight(getBitPairIndex(cell)).and(MASK_BITS);
        if (bits.equals(PLAYER_BITS.get(Piece.X))) {
            return Piece.X;
        } else if (bits.equals(PLAYER_BITS.get(Piece.O))) {
            return Piece.O;
        } else {
            return null;
        }
    }

    private int getBitPairIndex(Cell cell) {
        return 2*(cell.row*numCols + cell.column);
    }

    @Override
    public void set(Cell cell, Piece piece) {
        var oldPiece = get(cell);

        var i = getBitPairIndex(cell);
        var newState = boardState.andNot(MASK_BITS.shiftLeft(i)); // Set pair to 00
        if (piece != null) {
            newState = newState.or(PLAYER_BITS.get(piece).shiftLeft(i));
        }
        boardState = newState;


        if (piece != null && oldPiece == null) {
            numPieces = numPieces + 1;
        } else if (piece == null && oldPiece != null) {
            numPieces = numPieces - 1;
        }
    }

    @Override
    public int getNumPieces() {
        return numPieces;
    }

    @Override
    public Board copy() {
        var copy = new BigIntegerBoard(numRows, numCols);
        copy.numPieces = numPieces;
        copy.boardState = boardState; // BigInteger is immutable
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BigIntegerBoard that)) return false;
        return numRows == that.numRows && numCols == that.numCols && Objects.equals(boardState, that.boardState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardState, numRows, numCols);
    }
}
