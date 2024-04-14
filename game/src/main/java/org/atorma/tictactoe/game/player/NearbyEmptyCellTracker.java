package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NearbyEmptyCellTracker {
    private final int allowedDistance;
    private final Set<Cell> emptyCellsNearOccupied = new HashSet<>();

    public NearbyEmptyCellTracker(GameState initialState, int allowedDistance) {
        if (allowedDistance <= 0) {
            throw new IllegalArgumentException("Allowed distance " + allowedDistance + " invalid, must be >= 1");
        }
        this.allowedDistance = allowedDistance;

        init(initialState);
    }

    public int getAllowedDistance() {
        return allowedDistance;
    }

    public Set<Cell> getEmptyCellsNearOccupied() {
        return Collections.unmodifiableSet(this.emptyCellsNearOccupied);
    }

    private void init(GameState initialState) {
        for (int row = 0; row < initialState.getBoardRows(); row++) {
            for (int col = 0; col < initialState.getBoardCols(); col++) {
                Cell c = new Cell(row, col);
                if (!initialState.isAllowed(c)) {
                    updateNearbyEmptyCellsAroundCell(initialState, c);
                }
            }
        }
    }

    /**
     * Updates empty cells near the given occupied cell in the given state.
     * The state does not have to include the move that occupied the cell if that
     * is the next move.
     *
     * @param state
     * @param occupied
     */
    public void addOccupiedCell(GameState state, Cell occupied) {
        if (occupied == null) {
            return;
        }
        emptyCellsNearOccupied.remove(occupied);
        updateNearbyEmptyCellsAroundCell(state, occupied);
    }

    private void updateNearbyEmptyCellsAroundCell(GameState state, Cell cell) {
        int maxDistance = Math.min(
                Math.max(state.getBoardRows() -1, state.getBoardCols() - 1),
                allowedDistance
        );
        for (int d = 1; d <= maxDistance; d++) {
            Cell[] cells = {
                    new Cell(cell.row - d, cell.column - d),
                    new Cell(cell.row - d, cell.column),
                    new Cell(cell.row - d, cell.column + d),
                    new Cell(cell.row, cell.column - d),
                    new Cell(cell.row, cell.column + d),
                    new Cell(cell.row + d, cell.column - d),
                    new Cell(cell.row + d, cell.column),
                    new Cell(cell.row + d, cell.column + d)
            };

            for (Cell c : cells) {
                if (state.isAllowed(c)) {
                    emptyCellsNearOccupied.add(c);
                }
            }
        }
    }
}
