package org.atorma.tictactoe.game;

import org.atorma.tictactoe.MockitoTests;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameTests extends MockitoTests {

    @Mock private Player xPlayer;
    @Mock private Player oPlayer;

    @Before
    public void setUp() {
        when(xPlayer.getPiece()).thenReturn(Piece.X);
        when(oPlayer.getPiece()).thenReturn(Piece.O);
    }

    @Test
    public void create_game() {
        GameState initialState = new GameState(5, new Piece[18][18], Piece.X);

        Game game = new Game(xPlayer, oPlayer, initialState);

        assertSame(xPlayer, game.getPlayers().get(Piece.X));
        assertSame(oPlayer, game.getPlayers().get(Piece.O));
        assertEquals(initialState.getTurn(), game.getCurrentState().getTurn());
        assertEquals(1, game.getTurnNumber());
        assertNotNull(game.getId());
    }

}
