package org.atorma.tictactoe.game.player.mcts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

public class NearbyMovesFilter implements MoveFilter {
    @JsonProperty("maxDistance")
    private final int maxDistance;

    @JsonCreator
    public NearbyMovesFilter(@JsonProperty("maxDistance") int maxDistance) {
        this.maxDistance = maxDistance;
    }

    @Override
    public boolean isAllowed(GameState gameState, Cell move) {
        if (gameState.getNumPieces() == 0) {
            return true;
        }

        var minRow = Math.max(move.getRow() - maxDistance, 0);
        var maxRow = Math.min(move.getRow() + maxDistance, gameState.getBoardRows() - 1);
        var minCol = Math.max(move.getColumn() - maxDistance, 0);
        var maxCol = Math.min(move.getColumn() + maxDistance, gameState.getBoardCols() - 1);

        for (var row = minRow; row <= maxRow; row++) {
            for (var col = minCol; col <= maxCol; col++) {
                if (gameState.getPiece(row, col) != null
                        && Cell.getDistance(move, new Cell(row, col)) <= maxDistance) {
                    return true;
                }
            }
        }

        return false;
    }


}
