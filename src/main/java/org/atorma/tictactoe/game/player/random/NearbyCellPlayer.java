package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Base class for players that restrict moves to cells that are close to occupied cells.
 */
public abstract class NearbyCellPlayer implements Player {
    private final int allowedDistance;
    private final Set<Cell> cellsNearOccupied = new HashSet<>();
    protected GameState currentState;

    public NearbyCellPlayer(int allowedDistance) {
        if (allowedDistance <= 0) {
            throw new IllegalArgumentException("Allowed distance " + allowedDistance + " invalid, must be >= 1");
        }
        this.allowedDistance = allowedDistance;
    }

    public int getAllowedDistance() {
        return allowedDistance;
    }

    public Set<Cell> getCellsNearOccupied() {
        return Collections.unmodifiableSet(this.cellsNearOccupied);
    }

    @Override
    public Cell move(GameState updatedState, Cell opponentsLastMove) {
        if (this.currentState == null || this.currentState.getNumPieces() <= updatedState.getNumPieces()) {
            this.currentState = updatedState;
            startNewGame();
        } else {
            this.currentState = updatedState;
        }

        if (opponentsLastMove != null) {
            cellsNearOccupied.remove(opponentsLastMove);
            updateAllowedCellsAfterCellOccupied(opponentsLastMove);
        }

        Cell myMove = planMove();

        if (myMove != null) {
            updateAllowedCellsAfterCellOccupied(myMove);
            cellsNearOccupied.remove(myMove);
        }

        return myMove;
    }

    private void startNewGame() {
        cellsNearOccupied.clear();
        for (int i = 0; i < currentState.getBoardRows(); i++) {
            for (int j = 0; j < currentState.getBoardCols(); j++) {
                Cell c = new Cell(i, j);
                if (!currentState.isAllowed(c)) {
                    updateAllowedCellsAfterCellOccupied(c);
                }
            }
        }
    }

    private void updateAllowedCellsAfterCellOccupied(Cell occupied) {
        int startRow = Math.max(0, occupied.getRow() - allowedDistance);
        int endRow = Math.min(currentState.getBoardRows(), occupied.getRow() + allowedDistance);
        int startCol = Math.max(0, occupied.getColumn() - allowedDistance);
        int endCol = Math.min(currentState.getBoardCols(), occupied.getColumn() + allowedDistance);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Cell cell = new Cell(row, col);
                // Use distance to filter out cells that are misaligned. This becomes an issue when allowedDistance > 1.
                int distance = Cell.getDistance(occupied, cell);
                if (currentState.isAllowed(cell) && distance <= allowedDistance) {
                    cellsNearOccupied.add(cell);
                }
            }
        }
    }

    protected abstract Cell planMove();

}
