package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.state.Board;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class NearbyEmptyCellTracker {
    private static final Logger LOGGER = LoggerFactory.getLogger(MCTSPlayer.class);

    private final int allowedDistance;
    private final Set<Cell> emptyCellsNearOccupied = new HashSet<>();
    private final Board board;

    public NearbyEmptyCellTracker(Board board, int allowedDistance) {
        this.board = board;
        if (allowedDistance <= 0) {
            throw new IllegalArgumentException("Allowed distance " + allowedDistance + " invalid, must be >= 1");
        }
        this.allowedDistance = allowedDistance;

        init();
    }

    public int getAllowedDistance() {
        return allowedDistance;
    }

    public Set<Cell> getEmptyCellsNearOccupied() {
        return Collections.unmodifiableSet(this.emptyCellsNearOccupied);
    }

    private void init() {
        long startTime = System.currentTimeMillis();
        emptyCellsNearOccupied.clear();
        for (int i = 0; i < board.getNumRows(); i++) {
            for (int j = 0; j < board.getNumCols(); j++) {
                Cell c = new Cell(i, j);
                if (isOccupied(c)) {
                    updateNearbyEmptyCellsAroundCell(c);
                }
            }
        }
        LOGGER.trace("NearbyEmptyCellTracker initialised in {} ms", System.currentTimeMillis() - startTime);
    }

    public void addOccupiedCell(Cell cell) {
        if (cell == null) {
            return;
        }
        if (!isWithinBoard(cell)) {
            throw new IllegalArgumentException("Cell " + cell + " is not within board");
        }
        emptyCellsNearOccupied.remove(cell);
        board.set(cell, Piece.X); // Piece type is irrelevant
        updateNearbyEmptyCellsAroundCell(cell);
    }

    private void updateNearbyEmptyCellsAroundCell(Cell occupied) {
        long startTime = System.nanoTime();

        int startRow = Math.max(0, occupied.getRow() - allowedDistance);
        int endRow = Math.min(board.getNumRows(), occupied.getRow() + allowedDistance);
        int startCol = Math.max(0, occupied.getColumn() - allowedDistance);
        int endCol = Math.min(board.getNumCols(), occupied.getColumn() + allowedDistance);

        for (int row = startRow; row <= endRow; row++) {
            for (int col = startCol; col <= endCol; col++) {
                Cell cell = new Cell(row, col);
                if (!isWithinBoard(cell) || isOccupied(cell)) {
                    continue;
                }
                // Use distance to filter out cells that are misaligned. This becomes an issue when allowedDistance > 1.
                int distance = Cell.getDistance(occupied, cell);
                if (distance <= allowedDistance) {
                    emptyCellsNearOccupied.add(cell);
                }
            }
        }

        LOGGER.trace("Updated empty cells near {} in {} ns", occupied, System.nanoTime() - startTime);
    }

    private boolean isWithinBoard(Cell cell) {
        return cell.getRow() >= 0
                && cell.getRow() < board.getNumRows()
                && cell.getColumn() >= 0
                && cell.getColumn() < board.getNumCols();
    }

    private boolean isOccupied(Cell cell) {
        return board.get(cell) != null;
    }
}
