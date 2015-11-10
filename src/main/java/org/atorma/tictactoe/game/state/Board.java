package org.atorma.tictactoe.game.state;

public interface Board {

    int getNumRows();
    int getNumCols();

    Piece get(Cell cell);
    void set(Cell cell, Piece piece);

    Board copy();
}
