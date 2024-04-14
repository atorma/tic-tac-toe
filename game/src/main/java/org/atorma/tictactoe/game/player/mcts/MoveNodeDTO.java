package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.GameState;

import java.util.Map;
import java.util.List;

public record MoveNodeDTO(
        Cell cell,
        Piece nextPlayer,
        boolean isEndState,

        List<String> expandedChildren,

        Map<Piece, Integer> wins,
        Map<Piece, Double> rewardSums,
        int numPlays,

        RewardScheme rewardScheme,
        GameState rootState
) {
}
