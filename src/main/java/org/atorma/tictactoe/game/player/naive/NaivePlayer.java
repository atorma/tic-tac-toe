package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A player that tries to elongate its longest sequence,
 * takes a decisive move if possible and tries to block
 * the opponent's decisive move.
 */
public class NaivePlayer implements Player {
    private Piece mySide;
    private GameState currentState;

    public void setPiece(Piece p) {
        this.mySide = p;
    }

    public Piece getPiece() {
        return this.mySide;
    }

    public Cell move(GameState currentState, Cell opponentsLastMove) {
        this.currentState = currentState;

        return Stream.<Supplier<Optional<Cell>>>of(
                this::getMandatoryMove,
                this::elongateLongestSequence,
                this::elongateNextLongestSequence)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(this::pickRandomCell);
    }

    private Optional<Cell> getMandatoryMove() {
        for (Cell move : currentState.getAllowedMoves()) {

            // Will I win?
            if (currentState.next(move).getWinner() == mySide) {
                return Optional.of(move);
            }

            // Would my opponent win if she were me?
            GameState fakeState = GameState.builder()
                    .setTemplate(currentState)
                    .setNextPlayer(mySide.other())
                    .build();
            fakeState.update(move);
            if (fakeState.getWinner() == mySide.other()) {
                return Optional.of(move);
            }

        }

        return Optional.empty();
    }

    private Optional<Cell> elongateLongestSequence() {
        GameState.Sequence myLongestSequence = currentState.getLongestSequence(mySide);
        List<Cell> candidates = getFreeSequenceEnds(currentState, myLongestSequence);
        if (!candidates.isEmpty()) {
            return Optional.of(Utils.pickRandom(candidates));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Cell> elongateNextLongestSequence() {
        List<GameState.Sequence> mySequences = currentState.getAllSequences().get(mySide);
        return mySequences.stream()
                .sorted((o1, o2) -> o2.length - o1.length)
                .map(sequence -> getFreeSequenceEnds(currentState, sequence))
                .filter(sequenceEnds -> !sequenceEnds.isEmpty())
                .findFirst()
                .map(Utils::pickRandom);
    }

    private Cell pickRandomCell() {
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
