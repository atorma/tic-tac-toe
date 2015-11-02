package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

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
        params.players.player1 = playerRegistry.getPlayerInformation().stream()
                .filter(x -> x.getName().equals("Random"))
                .findFirst()
                .get();
        params.players.player2 = playerRegistry.getPlayerInformation().stream()
                .filter(x -> x.getName().equals("Naive"))
                .findFirst()
                .get();

        Game game = gameFactory.createGame(params);

        assertThat(game, notNullValue());
        assertThat(game.getTurnNumber(), is(1));
        assertThat(game.getState().getTurn(), is(params.firstPlayer));
        assertThat(game.getState().getBoardRows(), is(params.board.rows));
        assertThat(game.getState().getBoardCols(), is(params.board.columns));
        assertThat(game.getPlayers().get(Piece.X).getClass() == NaivePlayer.class && game.getPlayers().get(Piece.O).getClass() == RandomPlayer.class
                || game.getPlayers().get(Piece.O).getClass() == NaivePlayer.class && game.getPlayers().get(Piece.X).getClass() == RandomPlayer.class, is(true));
    }

}
