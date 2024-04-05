package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.NearbyEmptyCellTracker;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

import java.util.Collections;
import java.util.Set;

/**
 * Base class for players that restrict moves to cells that are close to occupied cells.
 */
public abstract class NearbyCellPlayer implements Player {
    private final int allowedDistance;
    protected GameState currentState;
    private NearbyEmptyCellTracker nearbyEmptyCellTracker;

    public NearbyCellPlayer(int allowedDistance) {
        if (allowedDistance <= 0) {
            throw new IllegalArgumentException("Allowed distance " + allowedDistance + " invalid, must be >= 1");
        }
        this.allowedDistance = allowedDistance;
    }

    public int getAllowedDistance() {
        return allowedDistance;
    }

    public Set<Cell> getEmptyCellsNearOccupied() {
        return nearbyEmptyCellTracker == null ? Collections.emptySet() : nearbyEmptyCellTracker.getEmptyCellsNearOccupied();
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
           nearbyEmptyCellTracker.addOccupiedCell(updatedState, opponentsLastMove);
        }

        Cell myMove = planMove();

        if (myMove != null) {
            nearbyEmptyCellTracker.addOccupiedCell(updatedState, myMove);
        }

        return myMove;
    }

    private void startNewGame() {
        nearbyEmptyCellTracker = new NearbyEmptyCellTracker(currentState, this.allowedDistance);
    }

    protected abstract Cell planMove();

}
