package org.atorma.tictactoe.game.player.mcts;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
@FunctionalInterface
public interface MoveFilter {

    /**
     * A function that further restricts allowed moves in the game.
     * The implementation does not need to check basic game rules
     * but e.g. restrict moves near occupied cells.
     *
     * @param gameState The game's state
     * @param move A move
     * @return true if taking the move in the game's state is allowed
     */
    boolean isAllowed(GameState gameState, Cell move);

}
