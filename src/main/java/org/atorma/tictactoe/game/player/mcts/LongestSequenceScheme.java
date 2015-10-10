package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;

public class LongestSequenceScheme implements RewardScheme {

    private final int connectHowMany;
    private final double uctConstant;

    public LongestSequenceScheme(int connectHowMany) {
        this(connectHowMany, 2000);
    }

    public LongestSequenceScheme(int connectHowMany, double uctBias) {
        this.connectHowMany = connectHowMany;
        this.uctConstant = uctBias;
    }

    public double getReward(Piece player, GameState gameState) {
        if (gameState.getWinner() == player) {
            return 1000;
        } else if (gameState.getWinner() == player.other()) {
            return -1000;
        } else {
            return gameState.getLongestSequence(player).length;
        }
    }

    public double getExplorationBonus(Piece player, MoveNode candidate) {
        return UCT.getUCTBonus(candidate, uctConstant);
    }
}
