package org.atorma.tictactoe.game.state;

public enum Piece {
    CROSS,
    ROUND;

    public Piece other() {
        return this == Piece.CROSS ? Piece.ROUND : Piece.CROSS;
    }
}
