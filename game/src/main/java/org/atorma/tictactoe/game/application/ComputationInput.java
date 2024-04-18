package org.atorma.tictactoe.game.application;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

/**
 * An object for transferring data to a tic-tac-toe move computation service.
 *
 * @param player    A player object, can contain state
 * @param state     The game's state
 * @param lastMove  The last move of the game (opponent's move)
 */
public record ComputationInput(Player player, GameState state, Cell lastMove) {}
