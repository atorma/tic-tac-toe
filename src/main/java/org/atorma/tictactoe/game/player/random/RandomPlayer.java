package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.Utils;


public class RandomPlayer implements Player {
    private Piece myPiece;


    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        return Utils.pickRandom(currentState.getAllowedMoves());
    }

    @Override
    public void setPiece(Piece p) {
        myPiece = p;
    }

    @Override
    public Piece getPiece() {
        return myPiece;
    }

    @Override
    public String toString() {
        return "Random";
    }



}
