package org.atorma.tictactoe.game;

import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class GameTests extends UnitTests {

    @Mock private Player xPlayer;
    @Mock private Player oPlayer;
    @Mock private GameState state;


    @Before
    public void setUp() {
        when(xPlayer.getPiece()).thenReturn(Piece.X);
        when(oPlayer.getPiece()).thenReturn(Piece.O);

        when(state.getCopy()).thenReturn(state);
    }

    @Test
    public void create_game() {
        Game game = new Game(xPlayer, oPlayer, state);

        assertSame(xPlayer, game.getPlayers().get(Piece.X));
        assertSame(oPlayer, game.getPlayers().get(Piece.O));
        assertEquals(state.getCopy(), game.getState());
        assertNull(game.getLastMove());
        assertEquals(1, game.getTurnNumber());
        assertNotNull(game.getId());
    }

    @Test
    public void play_turns() {
        GameState initialState = state;
        when(initialState.getTurn()).thenReturn(Piece.X);
        Game game = new Game(xPlayer, oPlayer, initialState);

        Cell xPlayerMove = new Cell(5, 5);
        when(xPlayer.move(initialState, null)).thenReturn(xPlayerMove);
        GameState afterXPlayerMove = mock(GameState.class);
        when(initialState.next(xPlayerMove)).thenReturn(afterXPlayerMove);
        when(afterXPlayerMove.getTurn()).thenReturn(Piece.O);

        game.playTurn();

        assertEquals(afterXPlayerMove, game.getState());
        assertEquals(xPlayerMove, game.getLastMove().getCell());
        assertEquals(2, game.getTurnNumber());

        Cell oPlayerMove = new Cell(6, 6);
        when(oPlayer.move(afterXPlayerMove, xPlayerMove)).thenReturn(oPlayerMove);
        GameState afterOPlayerMove = mock(GameState.class);
        when(afterXPlayerMove.next(oPlayerMove)).thenReturn(afterOPlayerMove);
        when(afterOPlayerMove.getTurn()).thenReturn(Piece.X);

        game.playTurn();

        assertEquals(afterOPlayerMove, game.getState());
        assertEquals(oPlayerMove, game.getLastMove().getCell());
        assertEquals(3, game.getTurnNumber());
    }
}
