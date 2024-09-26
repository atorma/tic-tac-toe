package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.Piece;

import java.util.*;

public class RandomNearbyPlayer extends NearbyCellPlayer implements Player {
    private Piece myPiece;

    public RandomNearbyPlayer() {
        this(1);
    }

    public RandomNearbyPlayer(int allowedDistance) {
        super(allowedDistance);
    }

    @Override
    protected Cell planMove() {
        List<Cell> candidates;
        if (getEmptyCellsNearOccupied().isEmpty()) {
            candidates = currentState.getAllowedMoves();
        } else {
            candidates = new ArrayList<>(getEmptyCellsNearOccupied());
        }

        if (candidates.isEmpty()) {
            return null;
        } else {
            Cell myMove = Utils.pickRandom(candidates);
            return myMove;
        }
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
        return "Random Adjacent";
    }
}
