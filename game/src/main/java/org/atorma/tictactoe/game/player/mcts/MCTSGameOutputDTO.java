package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.Cell;

/**
 * An object for transferring the result MCTS move from a computation service.
 *
 * @param move      The MCTS player's move
 * @param player    The MCTS player's state after the move
 */
public record MCTSGameOutputDTO(Cell move, MCTSPlayer player) {
}
