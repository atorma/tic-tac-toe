package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

/**
 * An object for transferring an MCTS game state to a computation service.
 *
 * @param lastMove  The opponent's last move
 * @param gameState The state of the game after lastMove
 * @param player    The MCTS player's state
 */
public record MCTSGameInputDTO(Cell lastMove, GameState gameState, MCTSPlayer player) {
}
