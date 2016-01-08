package org.atorma.tictactoe.game.player.naive;

import org.atorma.tictactoe.game.player.random.AdjacentCellPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Sequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MandatoryMovePlayer extends AdjacentCellPlayer {


    protected Optional<Cell> getMandatoryMove() {
        GameState fakeState = GameState.builder()
                .setTemplate(currentState)
                .setNextPlayer(getPiece().other())
                .build();

        // Can I win with one move?
        for (Cell move : adjacentToOccupied) {
            if (currentState.next(move).getWinner() == getPiece()) {
                return Optional.of(move);
            }
        }

        // Can my opponent win with one move? If yes, block the move.
        for (Cell move : adjacentToOccupied) {
            if (fakeState.next(move).getWinner() == getPiece().other()) {
                return Optional.of(move);
            }
        }

        // Can I make a sequence that will yield a victory in my next turn?
        for (Cell move : adjacentToOccupied) {
            GameState nextState = currentState.next(move);
            if (nextState.getUpdatedSequences().stream()
                    .anyMatch(sequence -> sequence.getLength() >= currentState.getConnectHowMany() - 1 && getFreeSequenceEnds(nextState, sequence).size() >= 2)) {
                return Optional.of(move);
            }
        }

        // Can my opponent make a sequence that would will a victory for her in her next turn? If yes, block the move.
        for (Cell move : adjacentToOccupied) {
            GameState fakeState2 = fakeState.next(move);
            if (fakeState2.getUpdatedSequences().stream()
                    .anyMatch(sequence -> sequence.getLength() >= currentState.getConnectHowMany() - 1 && getFreeSequenceEnds(fakeState2, sequence).size() >= 2)) {
                return Optional.of(move);
            }
        }

        return Optional.empty();
    }

    protected List<Cell> getFreeSequenceEnds(GameState state, Sequence sequence) {
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
}
