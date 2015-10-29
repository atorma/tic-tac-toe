package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A player that tries to elongate its longest sequence,
 * takes a decisive move if possible and tries to block
 * the opponent's decisive move.
 */
public class NaivePlayer implements Player {

    private Piece mySide;


    public void setPiece(Piece p) {
        this.mySide = p;
    }

    public Piece getPiece() {
        return this.mySide;
    }

    public Cell move(GameState currentState, Cell opponentsLastMove) {
        Cell winningMove = null;
        Cell opponentWinningMove = null;

        for (Cell move : currentState.getAllowedMoves()) {
            // Will I win?
            if (currentState.next(move).getWinner() == mySide) {
                winningMove = move;
                break;
            }

            // Would my opponent win if she were me?
            GameState fakeState = new GameState(currentState, mySide.other());
            fakeState.update(move);
            if (fakeState.getWinner() == mySide.other()) {
                opponentWinningMove =  move;
            }
        }

        // If I have a decisive move, take it
        if (winningMove != null) {
            return winningMove;
        }

        // If opponent would win, try to prevent it by stealing the move
        if (opponentWinningMove != null) {
            return opponentWinningMove;
        }

        // Otherwise elongate my longest sequence if possible
        // (longest sequence is already computed and cached)
        GameState.Sequence myLongestSequence = currentState.getLongestSequence(mySide);
        List<Cell> candidates = getFreeSequenceEnds(currentState, myLongestSequence);
        if (!candidates.isEmpty()) {
            return Utils.pickRandom(candidates);
        }

        // My longest sequence is blocked, elongate the next longest possible
        List<GameState.Sequence> mySequences = currentState.getAllSequences().get(mySide);
        Collections.sort(mySequences, (o1, o2) -> o2.length - o1.length);
        for (GameState.Sequence seq : mySequences) {
            candidates = getFreeSequenceEnds(currentState, seq);
            if (!candidates.isEmpty()) {
                return Utils.pickRandom(candidates);
            }
        }

        // If no sequences to elongate, pick random
        return Utils.pickRandom(currentState.getAllowedMoves());
    }

    private List<Cell> getFreeSequenceEnds(GameState state, GameState.Sequence sequence) {
        List<Cell> candidates = new ArrayList<>();

        if (sequence.length == 0) {
            return candidates;
        }

        // Row sequence
        if (sequence.start.getRow() == sequence.end.getRow()) {
            int row = sequence.start.getRow();

            int col = sequence.start.getColumn() - 1;
            if (col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            col = sequence.end.getColumn() + 1;
            if (col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        // Column sequence
        if (sequence.start.getColumn() == sequence.end.getColumn()) {
            int col = sequence.start.getColumn();

            int row = sequence.start.getRow() - 1;
            if (row >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.end.getRow() + 1;
            if (row < state.getBoardRows() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        // Left-right diagonal sequence
        if (sequence.length == 1 || (sequence.start.getRow() < sequence.end.getRow()
                && sequence.start.getColumn() < sequence.end.getColumn()) ) {

            int row = sequence.start.getRow() - 1;
            int col = sequence.start.getColumn() - 1;
            if (row >= 0 && col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.end.getRow() + 1;
            col = sequence.end.getColumn() + 1;
            if (row < state.getBoardRows() && col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        // Right-left diagonal sequence
        if (sequence.length == 1 || (sequence.start.getRow() < sequence.end.getRow()
                && sequence.start.getColumn() > sequence.end.getColumn()) ) {

            int row = sequence.start.getRow() - 1;
            int col = sequence.start.getColumn() + 1;
            if (row >= 0 && col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.end.getRow() + 1;
            col = sequence.end.getColumn() - 1;
            if (row < state.getBoardRows() && col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        return candidates;
    }

    @Override
    public String toString() {
        return "Naive";
    }
}
