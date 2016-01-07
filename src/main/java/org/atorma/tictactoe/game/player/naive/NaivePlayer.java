package org.atorma.tictactoe.game.player.naive;


import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.random.AdjacentCellPlayer;
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
public class NaivePlayer extends MandatoryMovePlayer implements Player {
    private Piece mySide;

    @Override
    protected Cell planMove() {
        return Stream.<Supplier<Optional<Cell>>>of(
                this::getMandatoryMove,
                this::getMoveElongatingLongestSequence,
                this::getMoveElongatingNextLongestSequence)
                .map(Supplier::get)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseGet(this::getRandomAllowedMove);
    }

    private Optional<Cell> getMoveElongatingLongestSequence() {
        Sequence myLongestSequence = currentState.getLongestSequence(mySide);
        List<Cell> candidates = getFreeSequenceEnds(currentState, myLongestSequence);
        if (!candidates.isEmpty()) {
            return Optional.of(Utils.pickRandom(candidates));
        } else {
            return Optional.empty();
        }
    }

    private Optional<Cell> getMoveElongatingNextLongestSequence() {
        List<Sequence> mySequences = currentState.getAllSequences().get(mySide);
        return mySequences.stream()
                .sorted((o1, o2) -> o2.getLength() - o1.getLength())
                .map(sequence -> getFreeSequenceEnds(currentState, sequence))
                .filter(sequenceEnds -> !sequenceEnds.isEmpty())
                .findFirst()
                .map(Utils::pickRandom);
    }

    private Cell getRandomAllowedMove() {
        return Utils.pickRandom(currentState.getAllowedMoves());
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
