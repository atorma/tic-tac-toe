package org.atorma.tictactoe.game.state;

public record DenseArrayBoardDTO(int numRows, int numCols, OccupiedCell[] occupiedCells) {}
