package org.atorma.tictactoe.game.player.naive;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Sequence;
import org.atorma.tictactoe.game.player.random.NearbyCellPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class MandatoryMovePlayer extends NearbyCellPlayer {

    public MandatoryMovePlayer(int nearbyCellDistance) {
        super(nearbyCellDistance);
    }

    protected Optional<Cell> getMandatoryMove() {
        GameState fakeState = GameState.builder()
                .setTemplate(currentState)
                .setNextPlayer(getPiece().other())
                .build();

        // Can I win with one move?
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (currentState.next(move).getWinner() == getPiece()) {
                return Optional.of(move);
            }
        }


        // Can my opponent win with one move? If yes, block the move.
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (fakeState.next(move).getWinner() == getPiece().other()) {
                return Optional.of(move);
            }
        }

        // Can I make a sequence that will yield a victory on my next turn?
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (isVictoryInTwoTurns(move, currentState)) {
                return Optional.of(move);
            }
        }

        // Can my opponent make a sequence that will yield a victory in her next turn? If yes, block it.
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (isVictoryInTwoTurns(move, fakeState)) {
                return Optional.of(move);
            }
        }

        // Can I make two sequences that can yield a victory on my third move
        // (e.g. two sequences of three cells with free ends in connect 5)?
        // Note that the victory may be delayed by creating more urgent moves to block.
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (isVictoryInThreeTurns(move, currentState)) {
                return Optional.of(move);
            }
        }

        // Same as above but for the opponent.
        for (Cell move : getEmptyCellsNearOccupied()) {
            if (isVictoryInThreeTurns(move, fakeState)) {
                return Optional.of(move);
            }
        }

        return Optional.empty();
    }

    private boolean isVictoryInTwoTurns(Cell move, GameState state) {
        GameState nextState = state.next(move);
        return nextState.getUpdatedSequences().stream()
                .anyMatch(sequence ->
                        sequence.getLength() >= state.getConnectHowMany() - 1 &&
                                getFreeSequenceEnds(nextState, sequence).size() >= 2
                );
    }

    private boolean isVictoryInThreeTurns(Cell move, GameState state) {
        GameState nextState = state.next(move);
        return nextState.getUpdatedSequences().stream()
                .filter(sequence ->
                        sequence.getLength() >= state.getConnectHowMany() - 2 &&
                                getFreeSequenceEnds(nextState, sequence).size() >= 2
                ).count() >= 2;
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
