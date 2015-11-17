package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

public class SequenceLengthDifferenceScheme implements RewardScheme {

    private final int connectHowMany;
    private final double uctConstant;

    public SequenceLengthDifferenceScheme(int connectHowMany) {
        this(connectHowMany, 2000);
    }

    public SequenceLengthDifferenceScheme(int connectHowMany, double uctConstant) {
        this.connectHowMany = connectHowMany;
        this.uctConstant = uctConstant;
    }

    public double getReward(Piece player, GameState gameState) {
        if (gameState.getWinner() == player) {
            return 1000;
        } else if (gameState.getWinner() == player.other()) {
            return -1000;
        } else {
            double lengthDiff = gameState.getLongestSequence(player).length - gameState.getLongestSequence(player.other()).length;
            return lengthDiff;
        }
    }


    public double getExplorationBonus(Piece player, MoveNode candidate) {
        return UCT.getUCTBonus(candidate, uctConstant);
    }
}
