package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.state.Board;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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
        for (int row = 0; row < board.getNumRows(); row++) {
            for (int col = 0; col < board.getNumCols(); col++) {
                Cell c = new Cell(row, col);
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
            throw new IllegalArgumentException(cell + " is not within board");
        }
        emptyCellsNearOccupied.remove(cell);
        board.set(cell, Piece.X); // Piece type is irrelevant
        updateNearbyEmptyCellsAroundCell(cell);
    }

    private void updateNearbyEmptyCellsAroundCell(Cell cell) {
        long startTime = System.nanoTime();

        for (int d = 1; d <= allowedDistance; d++) {
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
                if (isAllowed(c)) {
                    emptyCellsNearOccupied.add(c);
                }
            }
        }

        LOGGER.trace("Updated empty cells near {} in {} ns", cell, System.nanoTime() - startTime);
    }

    private boolean isAllowed(Cell cell) {
        return isWithinBoard(cell) && isFree(cell);
    }

    private boolean isWithinBoard(Cell cell) {
        return cell.row >= 0
                && cell.row < board.getNumRows()
                && cell.column >= 0
                && cell.column < board.getNumCols();
    }

    private boolean isFree(Cell cell) {
        return board.get(cell) == null;
    }

    private boolean isOccupied(Cell cell) {
        return board.get(cell) != null;
    }
}
