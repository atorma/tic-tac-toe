package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.SlowTests;
import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.Simulator;
import org.atorma.tictactoe.game.player.random.RandomNearbyPlayer;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Category(SlowTests.class)
public class RandomNearbyPlayerPerformanceTests extends UnitTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(RandomNearbyPlayerPerformanceTests.class);

    @Test
    public void test_performance() {
        RandomNearbyPlayer player1 = new RandomNearbyPlayer(1);
        player1.setPiece(Piece.X);
        RandomNearbyPlayer player2 = new RandomNearbyPlayer(1);
        player2.setPiece(Piece.O);

        long nRounds = 10000;
        long startTime = System.currentTimeMillis();
        for (int round = 1; round <= nRounds; round++) {
            GameState startingState = GameState.builder().setConnectHowMany(5)
                    .setBoard(new Piece[18][18])
                    .setNextPlayer(player1.getPiece())
                    .build();
            Simulator simulator = new Simulator(startingState, player1, player2);
            simulator.run();
        }
        long duration = System.currentTimeMillis() - startTime;
        LOGGER.info("{} ms", duration);
    }
}
