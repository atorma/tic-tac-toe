package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;

public class WinLossDrawScheme implements RewardScheme {

    private final double uctConstant;

    public WinLossDrawScheme() {
        this(2/Math.sqrt(2));
    }

    public WinLossDrawScheme(double uctConstant) {
        this.uctConstant = uctConstant;
    }

    public double getReward(Piece player, GameState gameState) {
        if (player == gameState.getWinner()) {
            return 1;
        } else if (player.other() == gameState.getWinner()) {
            return -1;
        } else {
            return 0;
        }
    }

    public double getExplorationBonus(Piece player, MoveNode candidate) {
        return UCT.getUCTBonus(candidate, uctConstant);
    }
}
