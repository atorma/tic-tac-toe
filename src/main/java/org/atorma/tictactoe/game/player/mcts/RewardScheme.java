package org.atorma.tictactoe.game.player.mcts;


import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;

public interface RewardScheme {

    /**
     * Returns the reward to a player if/when the game reaches gameState.
     */
    double getReward(Piece player, GameState gameState);

    /**
     * Returns an exploration  bonus if the player would choose given move candidate.
     *
     * The purpose of this is to balance game tree exploitation versus exploration.
     * For example, this could be an Upper Confidence Bound applied to Trees (UCT).
     */
    double getExplorationBonus(Piece player, MoveNode candidate);

}
