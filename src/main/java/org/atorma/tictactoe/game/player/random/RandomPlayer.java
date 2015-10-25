package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;


public class RandomPlayer implements Player {
    private Piece myPiece;
    private String name;

    public RandomPlayer(String name) {
        this.name = name;
    }

    public RandomPlayer() {
        this("Random");
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
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        return Utils.pickRandom(currentState.getAllowedMoves());
    }

    @Override
    public String toString() {
        return name;
    }



}
