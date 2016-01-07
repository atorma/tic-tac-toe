package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

import java.util.HashSet;
import java.util.Set;

public abstract class AdjacentCellPlayer implements Player {

    protected Set<Cell> adjacentToOccupied;
    protected GameState currentState;

    @Override
    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (this.currentState == null || this.currentState.getNumPieces() <= updatedState.getNumPieces()) {
            this.currentState = updatedState;
            startNewGame();
        } else {
            this.currentState = updatedState;
        }

        if (opponentsLastMove != null) {
            adjacentToOccupied.remove(opponentsLastMove);
            collectAdjacentToOccupied(opponentsLastMove);
        }

        Cell myMove = planMove();

        if (myMove != null) {
            collectAdjacentToOccupied(myMove);
            adjacentToOccupied.remove(myMove);
        }

        return myMove;
    }

    private void startNewGame() {
        adjacentToOccupied = new HashSet<>();
        for (int i = 0; i < currentState.getBoardRows(); i++) {
            for (int j = 0; j < currentState.getBoardCols(); j++) {
                Cell c = new Cell(i, j);
                if (!currentState.isAllowed(c)) {
                    collectAdjacentToOccupied(c);
                }
            }
        }
    }

    private void collectAdjacentToOccupied(Cell occupied) {
        Cell[] adjacent = new Cell[8];
        adjacent[0] = new Cell(occupied.getRow() - 1, occupied.getColumn() - 1);
        adjacent[1] = new Cell(occupied.getRow() - 1, occupied.getColumn());
        adjacent[2] = new Cell(occupied.getRow() - 1, occupied.getColumn() + 1);
        adjacent[3] = new Cell(occupied.getRow(), occupied.getColumn() - 1);
        adjacent[4] = new Cell(occupied.getRow(), occupied.getColumn() + 1);
        adjacent[5] = new Cell(occupied.getRow() + 1, occupied.getColumn() - 1);
        adjacent[6] = new Cell(occupied.getRow() + 1, occupied.getColumn());
        adjacent[7] = new Cell(occupied.getRow() + 1, occupied.getColumn() + 1);

        for (Cell c : adjacent) {
            if (currentState.isAllowed(c)) {
                adjacentToOccupied.add(c);
            }
        }
    }

    protected abstract Cell planMove();

}
