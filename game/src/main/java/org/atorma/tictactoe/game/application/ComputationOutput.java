package org.atorma.tictactoe.game.application;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;

/**
 * An output of a tic-tac-toe move computation.
 * The output does not include game state to save on network IO.
 * It is assumed that the caller stores the game state.
 *
 * @param player    A player object, can contain state
 * @param move      The player's move
 */
public record ComputationOutput(Player player, Cell move) {}
