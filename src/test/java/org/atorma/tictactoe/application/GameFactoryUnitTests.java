package org.atorma.tictactoe.application;

import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GameFactoryUnitTests extends UnitTests {

    GameFactory gameFactory;

    @Mock PlayerRegistry playerRegistry;
    @Mock Player player1;
    @Mock Player player2;

    @Before
    public void setUp() {
        gameFactory = new GameFactory(playerRegistry);

        when(player1.getPiece()).thenReturn(Piece.O);
        when(player2.getPiece()).thenReturn(Piece.X);
    }

    @Test
    public void create_game() {
        GameParams params = new GameParams();
        params.connectHowMany = 3;
        params.firstPlayer = Piece.O;
        params.board.rows = 3;
        params.board.columns = 4;
        params.players.player1 = mock(PlayerInfo.class);
        params.players.player2 = mock(PlayerInfo.class);

        when(playerRegistry.createPlayer(params.players.player1)).thenReturn(player1);
        when(playerRegistry.createPlayer(params.players.player2)).thenReturn(player2);

        Game game = gameFactory.createGame(params);

        assertThat(game, notNullValue());
        assertThat(game.getTurnNumber(), is(1));
        assertThat(game.getState().getTurn(), is(params.firstPlayer));
        assertThat(game.getState().getBoardRows(), is(params.board.rows));
        assertThat(game.getState().getBoardCols(), is(params.board.columns));
        assertThat(game.getPlayers().containsValue(player1), is(true));
        assertThat(game.getPlayers().containsValue(player2), is(true));
    }

}
