package org.atorma.tictactoe.game.player.mcts;

import org.atorma.tictactoe.SlowTests;
import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

@Category(SlowTests.class)
public class AiPlayTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiPlayTests.class);

    // Last results: MCTS Naive heuristics: 26 wins, MCTS uniform random: 24 wins, draws: 0
    @Test
    public void mcts_naive_vs_mcts_uniform_random() {
        MCTSParameters naiveParams = new MCTSParameters();
        naiveParams.simulationStrategy = MCTSParameters.SimulationStrategy.NAIVE;
        MCTSPlayer naiveMctsPlayer = new MCTSPlayer(naiveParams);
        naiveMctsPlayer.setPiece(Piece.X);

        MCTSParameters uniformRandomParams = new MCTSParameters();
        uniformRandomParams.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        MCTSPlayer uniformRandomMctsPlayer = new MCTSPlayer(uniformRandomParams);
        uniformRandomMctsPlayer.setPiece(Piece.O);

        Map<Piece, Integer> winsMap = new EnumMap<>(Piece.class);
        winsMap.put(Piece.X, 0);
        winsMap.put(Piece.O, 0);

        int nRounds = 50;
        for (int round = 0; round < nRounds; round++) {
            LOGGER.info("Starting round {}", round);
            MCTSPlayer startingPlayer = Math.random() < 0.5 ? naiveMctsPlayer : uniformRandomMctsPlayer;
            GameState startingState = GameState.builder().setConnectHowMany(5)
                    .setBoard(new Piece[18][18])
                    .setNextPlayer(startingPlayer.getPiece())
                    .build();
            Simulator simulator = new Simulator(startingState, naiveMctsPlayer, uniformRandomMctsPlayer);
            GameState endState = simulator.run();
            Piece winner = endState.getWinner();
            Integer wins = winsMap.get(winner);
            int updatedWins = wins + 1;
            winsMap.put(winner, updatedWins);
            LOGGER.info("Winner of round {}: {}, now has {} wins", round, winner, updatedWins);
        }


        LOGGER.info("MCTS Naive heuristics: {} wins, MCTS uniform random: {} wins, draws: {}",
                winsMap.get(naiveMctsPlayer.getPiece()),
                winsMap.get(uniformRandomMctsPlayer.getPiece()),
                nRounds - winsMap.get(naiveMctsPlayer.getPiece()) - winsMap.get(uniformRandomMctsPlayer.getPiece()));
    }

}
