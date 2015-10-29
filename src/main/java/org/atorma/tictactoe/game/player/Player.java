package org.atorma.tictactoe.game.player;


import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

/**
 * Interface of Tic-tac-toe AI implementations.
 */
public interface Player {

    /**
     * Called by the game framework when this player should make its move.
     * <p/>
     * If the number of pieces in the current state is less than in the
     * previous state, the player should take it as a signal that
     * a new game has started.
     *
     * @param currentState
     *  The current state of the game, including the opponent's last move
     * @param opponentsLastMove
     *  The previous move made by the opponent, null if this player
     *  has the first move of the game
     * @return
     *  The board cell where this player's next move should be placed
     */
    Cell move(GameState currentState, Cell opponentsLastMove);

    /**
     * Sets piece this player has.
     */
    void setPiece(Piece p);

    /**
     * This should always return the piece this player been set to play.
     *
     * @return
     *  The piece given with {@link #setPiece(Piece)}
     */
    Piece getPiece();

}
