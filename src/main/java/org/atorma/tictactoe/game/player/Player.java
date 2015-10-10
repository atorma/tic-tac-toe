package org.atorma.tictactoe.game.player;


import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

/**
 * A computer player.
 */
public interface Player {
    /**
     * This method is called by the game framework to notify you of your side.
     */
    void setSide(Piece p);

    /**
     * This should always return the side that you've been informed with by the setSide-method
     *
     * @return Piece given with setSide-method
     */
    Piece getSide();

    /**
     * @return your player's name
     */
    String getName();

    /**
     * @return your player's name
     */
    String toString();

    /**
     * Thi is your main implementation point. This method is called to determine your next move.
     * The given parameters are informational and you can modify them if you want, your
     * modifications will be ignored. The result of your algorithm is announced with the return
     * value.
     *
     * @param currentState
     *  The current state of the game, including the opponent's last move
     * @param opponentsLastMove
     *  A helper parameter to inform you about the last move made by your opponent.
     * @return
     *  Cell instance that describes on which board cell you want to place your piece.
     */
    Cell move(GameState currentState, Cell opponentsLastMove);

}
