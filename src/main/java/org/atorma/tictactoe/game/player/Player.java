package org.atorma.tictactoe.game.player;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

public interface Player {

    /**
     * This method is called by the game framework to set the piece this player is playing.
     */
    void setPiece(Piece p);

    /**
     * This should always return the piece this player been set to play.
     *
     * @return
     *  The piece given with {@link #setPiece(Piece)}
     */
    Piece getPiece();

    /**
     * @return
     *  This player's name
     */
    String getName();

    /**
     * @return
     *  This player's name
     */
    String toString();

    /**
     * Called by the game framework when this player should make its move.
     *
     * @param currentState
     *  The current state of the game, including the opponent's last move
     * @param opponentsLastMove
     *  The previous move made by the opponent
     * @return
     *  The board cell where this player's next move should be placed
     */
    Cell move(GameState currentState, Cell opponentsLastMove);

}
