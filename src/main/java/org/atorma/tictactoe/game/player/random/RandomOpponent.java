package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;


public class RandomOpponent implements Player {
    private Piece myPiece;
    private String name;

    public RandomOpponent(String name) {
        this.name = name;
    }

    @Override
    public void setSide(Piece p) {
        myPiece = p;
    }

    @Override
    public Piece getSide() {
        return myPiece;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        return Utils.pickRandom(currentState.getAllowedMoves());
    }

    @Override
    public String toString() {
        return getName();
    }



}
