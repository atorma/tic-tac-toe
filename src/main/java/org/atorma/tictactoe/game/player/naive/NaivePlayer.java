package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.random.AdjancentCellPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.Utils;
import org.atorma.tictactoe.game.state.Sequence;

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
public class NaivePlayer extends AdjancentCellPlayer implements Player {
    private Piece mySide;

    @Override
    protected Cell planMove() {
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
        GameState fakeState = GameState.builder()
                .setTemplate(currentState)
                .setNextPlayer(mySide.other())
                .build();

        for (Cell move : adjacentToOccupied) {
            // Will I win?
            if (currentState.next(move).getWinner() == mySide) {
                return Optional.of(move);
            }

            // Would my opponent win if she were me?
            if (fakeState.next(move).getWinner() == mySide.other()) {
                return Optional.of(move);
            }
        }

        for (Cell move : adjacentToOccupied) {
            // Will I get a sequence that will yield a victory in my next turn?
            GameState nextState = currentState.next(move);
            if (nextState.getUpdatedSequences().stream()
                    .anyMatch(sequence -> sequence.getLength() >= 4 && getFreeSequenceEnds(nextState, sequence).size() >= 2)) {
                return Optional.of(move);
            }

            // Would my opponent get a sequence that would yield a victory for her if she were me?
            GameState fakeState2 = fakeState.next(move);
            if (fakeState2.getUpdatedSequences().stream()
                    .anyMatch(sequence -> sequence.getLength() >= 4 && getFreeSequenceEnds(fakeState2, sequence).size() >= 2)) {
                return Optional.of(move);
            }
        }

        return Optional.empty();
    }

    private Optional<Cell> elongateLongestSequence() {
        Sequence myLongestSequence = currentState.getLongestSequence(mySide);
        List<Cell> candidates = getFreeSequenceEnds(currentState, myLongestSequence);
        if (!candidates.isEmpty()) {
            return Optional.of(Utils.pickRandom(candidates));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Cell> elongateNextLongestSequence() {
        List<Sequence> mySequences = currentState.getAllSequences().get(mySide);
        return mySequences.stream()
                .sorted((o1, o2) -> o2.getLength() - o1.getLength())
                .map(sequence -> getFreeSequenceEnds(currentState, sequence))
                .filter(sequenceEnds -> !sequenceEnds.isEmpty())
                .findFirst()
                .map(Utils::pickRandom);
    }

    private Cell pickRandomCell() {
        return Utils.pickRandom(currentState.getAllowedMoves());
    }

    private List<Cell> getFreeSequenceEnds(GameState state, Sequence sequence) {
        List<Cell> candidates = new ArrayList<>();

        if (sequence.getLength() == 0) {
            return candidates;
        }

        if (sequence.hasDirection(Sequence.Direction.HORIZONTAL)) {
            int row = sequence.getStart().getRow();

            int col = sequence.getStart().getColumn() - 1;
            if (col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            col = sequence.getEnd().getColumn() + 1;
            if (col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        if (sequence.hasDirection(Sequence.Direction.VERTICAL)) {
            int col = sequence.getStart().getColumn();

            int row = sequence.getStart().getRow() - 1;
            if (row >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.getEnd().getRow() + 1;
            if (row < state.getBoardRows() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        if (sequence.hasDirection(Sequence.Direction.LEFT_RIGHT_DIAGONAL)) {

            int row = sequence.getStart().getRow() - 1;
            int col = sequence.getStart().getColumn() - 1;
            if (row >= 0 && col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.getEnd().getRow() + 1;
            col = sequence.getEnd().getColumn() + 1;
            if (row < state.getBoardRows() && col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        if (sequence.hasDirection(Sequence.Direction.RIGHT_LEFT_DIAGONAL)) {

            int row = sequence.getStart().getRow() - 1;
            int col = sequence.getStart().getColumn() + 1;
            if (row >= 0 && col < state.getBoardCols() && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }

            row = sequence.getEnd().getRow() + 1;
            col = sequence.getEnd().getColumn() - 1;
            if (row < state.getBoardRows() && col >= 0 && state.getPiece(row, col) == null) {
                candidates.add(new Cell(row, col));
            }
        }

        return candidates;
    }


    public void setPiece(Piece p) {
        this.mySide = p;
    }

    public Piece getPiece() {
        return this.mySide;
    }

    @Override
    public String toString() {
        return "Naive";
    }
}
