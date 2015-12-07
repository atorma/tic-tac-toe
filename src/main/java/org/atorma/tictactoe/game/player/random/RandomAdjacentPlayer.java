package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

import java.util.List;
import java.util.stream.Collectors;

public class RandomAdjacentPlayer implements Player {
    private Piece myPiece;

    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        List<Cell> candidates = currentState.getAllowedMoves()
                .stream()
                .filter(c -> hasAdjacentPiece(c, currentState))
                .collect(Collectors.toList());
        if (candidates.size() > 0) {
            return Utils.pickRandom(candidates);
        } else {
            return Utils.pickRandom(currentState.getAllowedMoves());
        }
    }

    private boolean hasAdjacentPiece(Cell cell, GameState state) {
        try {
            if (state.getPiece(cell.getRow() - 1, cell.getColumn() - 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow() - 1, cell.getColumn()) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow() - 1, cell.getColumn() + 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow(), cell.getColumn() - 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow(), cell.getColumn() + 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow() + 1, cell.getColumn() - 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow() + 1, cell.getColumn()) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        try {
            if (state.getPiece(cell.getRow() + 1, cell.getColumn() + 1) != null) {
                return true;
            }
        } catch (ArrayIndexOutOfBoundsException e) {}

        return false;
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
