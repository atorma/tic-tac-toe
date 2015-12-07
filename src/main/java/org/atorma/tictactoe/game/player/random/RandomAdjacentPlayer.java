package org.atorma.tictactoe.game.player.random;

import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

public class RandomAdjacentPlayer implements Player {
    private Piece myPiece;
    private List<Cell> centroids = new ArrayList<>();
    private GameState currentState;

    @Override
    public Cell move(GameState currentState, Cell opponentsLastMove) {
        this.currentState = currentState;
        if (opponentsLastMove != null) {
            centroids.add(opponentsLastMove);
        }

        return planMove();
    }

    private Cell planMove() {
        Cell myMove;

        List<Cell> candidates = new ArrayList<>();
        ListIterator<Cell> centroidIter = centroids.listIterator();
        while (centroidIter.hasNext()) {
            Cell centroid = centroidIter.next();
            List<Cell> adjacentCells = new ArrayList<>(8);
            adjacentCells.add(new Cell(centroid.getRow() - 1, centroid.getColumn() - 1));
            adjacentCells.add(new Cell(centroid.getRow() - 1, centroid.getColumn()));
            adjacentCells.add(new Cell(centroid.getRow() - 1, centroid.getColumn() + 1));
            adjacentCells.add(new Cell(centroid.getRow(), centroid.getColumn() - 1));
            adjacentCells.add(new Cell(centroid.getRow(), centroid.getColumn() + 1));
            adjacentCells.add(new Cell(centroid.getRow() + 1, centroid.getColumn() - 1));
            adjacentCells.add(new Cell(centroid.getRow() + 1, centroid.getColumn()));
            adjacentCells.add(new Cell(centroid.getRow() + 1, centroid.getColumn() + 1));
            List<Cell> unoccupiedNeighbors = adjacentCells.stream()
                    .filter(c -> c.getRow() >= 0 && c.getRow() < currentState.getBoardRows()
                            && c.getColumn() >= 0 && c.getColumn() < currentState.getBoardCols()
                            && currentState.getPiece(c) == null)
                    .collect(Collectors.toList());

            if (unoccupiedNeighbors.isEmpty()) {
                centroidIter.remove();
            } else {
                candidates.addAll(unoccupiedNeighbors);
            }
        }

        if (candidates.isEmpty()) {
            myMove = Utils.pickRandom(currentState.getAllowedMoves());
        } else {
            myMove = Utils.pickRandom(candidates);
        }
        centroids.add(myMove);

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
