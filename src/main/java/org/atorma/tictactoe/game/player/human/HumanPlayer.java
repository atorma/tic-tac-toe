package org.atorma.tictactoe.game.player.human;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

/**
 * A Player whose next move is set manually and is reset
 * after the move is completed.
 */
public class HumanPlayer implements Player {

    private Piece piece;
    private Cell nextMove;

    /**
     * Sets the move to take when {@link #move(GameState, Cell)} is called.
     *
     * @param nextMove
     *  the next move
     */
    public void setNextMove(Cell nextMove) {
        this.nextMove = nextMove;
    }

    /**
     * @return the move set using {@link #setNextMove(Cell)}
     */
    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        if (nextMove == null) {
            throw new IllegalStateException("Next move is not set");
        }
        Cell move = nextMove;
        nextMove = null;
        return move;
    }

    @Override
    public void setPiece(Piece p) {
        this.piece = p;
    }

    @Override
    public Piece getPiece() {
        return this.piece;
    }

    @Override
    public String toString() {
        return "Human";
    }
}
