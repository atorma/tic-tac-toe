package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

public class AllMovesAllowedFilter implements MoveFilter {
    @Override
    public boolean isAllowed(GameState gameState, Cell move) {
        return true;
    }
}
