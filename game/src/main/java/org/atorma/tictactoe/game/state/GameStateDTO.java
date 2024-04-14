package org.atorma.tictactoe.game.state;

import java.util.Set;

public record GameStateDTO(
        int connectHowMany,
        Board board,
        Piece nextPlayer,
        Set<Sequence> updatedSequences) { }
