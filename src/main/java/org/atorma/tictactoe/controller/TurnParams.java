package org.atorma.tictactoe.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.atorma.tictactoe.game.state.Cell;

public class TurnParams {
    public final int turnNumber;
    public final Cell move;

    @JsonCreator
    public TurnParams(@JsonProperty("turnNumber") int turnNumber, @JsonProperty("move") Cell move) {
        this.turnNumber = turnNumber;
        this.move = move;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public Cell getMove() {
        return move;
    }
}
