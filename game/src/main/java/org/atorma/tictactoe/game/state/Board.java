package org.atorma.tictactoe.game.state;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public interface Board {

    int getNumRows();
    int getNumCols();

    Piece get(Cell cell);
    void set(Cell cell, Piece piece);

    int getNumPieces();

    Board copy();
}
