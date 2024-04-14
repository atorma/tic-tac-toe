package org.atorma.tictactoe.game.state;

public enum Piece {
    X, O;

    public Piece other() {
        return this == Piece.X ? Piece.O : Piece.X;
    }
}
