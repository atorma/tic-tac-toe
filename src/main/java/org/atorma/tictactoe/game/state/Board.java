package org.atorma.tictactoe.game.state;

public interface Board {

    int getNumRows();
    int getNumCols();

    Piece get(int row, int col);
    void set(int row, int col, Piece piece);

    Board copy();
}
