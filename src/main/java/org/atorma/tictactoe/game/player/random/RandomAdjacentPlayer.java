package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

import java.util.*;

public class RandomAdjacentPlayer implements Player {
    private Piece myPiece;
    private Set<Cell> adjacentToOccupied;
    private GameState currentState;

    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        if (this.currentState == null || this.currentState.getNumPieces() < currentState.getNumPieces()) {
            this.currentState = currentState;
            startNewGame();
        } else {
            this.currentState = currentState;
        }

        if (opponentsLastMove != null) {
            adjacentToOccupied.remove(opponentsLastMove);
            collectAdjacentToOccupied(opponentsLastMove);
        }

        Cell myMove = planMove();

        collectAdjacentToOccupied(myMove);
        adjacentToOccupied.remove(myMove);

        return myMove;
    }

    private void startNewGame() {
        adjacentToOccupied = new HashSet<>();
        for (int i = 0; i < currentState.getBoardRows(); i++) {
            for (int j = 0; j < currentState.getBoardCols(); j++) {
                Cell c = new Cell(i, j);
                if (!currentState.isAllowed(c)) {
                    collectAdjacentToOccupied(c);
                }
            }
        }
    }

    private void collectAdjacentToOccupied(Cell occupied) {
        Cell[] adjacent = new Cell[8];
        adjacent[0] = new Cell(occupied.getRow() - 1, occupied.getColumn() - 1);
        adjacent[1] = new Cell(occupied.getRow() - 1, occupied.getColumn());
        adjacent[2] = new Cell(occupied.getRow() - 1, occupied.getColumn() + 1);
        adjacent[3] = new Cell(occupied.getRow(), occupied.getColumn() - 1);
        adjacent[4] = new Cell(occupied.getRow(), occupied.getColumn() + 1);
        adjacent[5] = new Cell(occupied.getRow() + 1, occupied.getColumn() - 1);
        adjacent[6] = new Cell(occupied.getRow() + 1, occupied.getColumn());
        adjacent[7] = new Cell(occupied.getRow() + 1, occupied.getColumn() + 1);

        for (Cell c : adjacent) {
            if (currentState.isAllowed(c)) {
                adjacentToOccupied.add(c);
            }
        }
    }

    private Cell planMove() {
        List<Cell> candidates;
        if (adjacentToOccupied.isEmpty()) {
            candidates = currentState.getAllowedMoves();
        } else {
            candidates = new ArrayList<>(adjacentToOccupied);
        }

        Cell myMove = Utils.pickRandom(candidates);

        return myMove;
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
