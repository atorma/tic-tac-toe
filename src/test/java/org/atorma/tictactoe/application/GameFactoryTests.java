package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(FastTests.class)
public class GameFactoryTests extends ApplicationTests {

    @Autowired GameFactory gameFactory;

    @Autowired PlayerRegistry playerRegistry;


    @Test
    public void create_game() {
        GameParams params = new GameParams();
        params.connectHowMany = 3;
        params.firstPlayer = Piece.O;
        params.board.rows = 3;
        params.board.columns = 4;
        params.players.put(Piece.X, playerRegistry.getPlayerInfoList().stream()
                .filter(x -> x.getName().equals("Random"))
                .findFirst()
                .get()
        );
        params.players.put(Piece.O, playerRegistry.getPlayerInfoList().stream()
                .filter(x -> x.getName().equals("Naive"))
                .findFirst()
                .get()
        );

        Game game = gameFactory.createGame(params);

        assertThat(game, notNullValue());
        assertThat(game.getTurnNumber(), is(1));
        assertThat(game.getState().getNextPlayer(), is(params.firstPlayer));
        assertThat(game.getState().getBoardRows(), is(params.board.rows));
        assertThat(game.getState().getBoardCols(), is(params.board.columns));
        assertThat(game.getPlayers().get(Piece.X), instanceOf(RandomPlayer.class));
        assertThat(game.getPlayers().get(Piece.O), instanceOf(NaivePlayer.class));
    }

}
