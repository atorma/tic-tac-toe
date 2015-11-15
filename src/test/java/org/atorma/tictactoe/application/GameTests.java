package org.atorma.tictactoe.application;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.controller.TurnParams;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@Category(FastTests.class)
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
        when(initialState.getNextPlayer()).thenReturn(Piece.X);
        Game game = new Game(xPlayer, oPlayer, initialState);

        Cell xPlayerMove = new Cell(5, 5);
        when(xPlayer.move(initialState, null)).thenReturn(xPlayerMove);
        GameState afterXPlayerMove = mock(GameState.class);
        when(initialState.next(xPlayerMove)).thenReturn(afterXPlayerMove);
        when(afterXPlayerMove.getNextPlayer()).thenReturn(Piece.O);

        game.playTurn(new TurnParams(1, new Cell(1, 1)));

        assertEquals(afterXPlayerMove, game.getState());
        assertEquals(xPlayerMove, game.getLastMove().getCell());
        assertEquals(2, game.getTurnNumber());

        Cell oPlayerMove = new Cell(6, 6);
        when(oPlayer.move(afterXPlayerMove, xPlayerMove)).thenReturn(oPlayerMove);
        GameState afterOPlayerMove = mock(GameState.class);
        when(afterXPlayerMove.next(oPlayerMove)).thenReturn(afterOPlayerMove);
        when(afterOPlayerMove.getNextPlayer()).thenReturn(Piece.X);

        game.playTurn(new TurnParams(2, new Cell(0, 0)));

        assertEquals(afterOPlayerMove, game.getState());
        assertEquals(oPlayerMove, game.getLastMove().getCell());
        assertEquals(3, game.getTurnNumber());
    }

    @Test
    public void when_next_player_is_human_then_sets_next_move_from_turn_params() {
        HumanPlayer humanPlayer = mock(HumanPlayer.class);
        when(humanPlayer.getPiece()).thenReturn(Piece.X);

        GameState initialState = state;
        when(initialState.getNextPlayer()).thenReturn(Piece.X);
        Game game = new Game(humanPlayer, oPlayer, initialState);

        Cell humanPlayerMove = new Cell(5, 5);
        when(humanPlayer.move(initialState, null)).thenReturn(humanPlayerMove);
        GameState nextState = mock(GameState.class);
        when(initialState.next(humanPlayerMove)).thenReturn(nextState);
        when(nextState.getNextPlayer()).thenReturn(Piece.O);

        game.playTurn(new TurnParams(1, humanPlayerMove));

        verify(humanPlayer).setNextMove(humanPlayerMove);
    }

    @Test(expected = TicTacToeException.class)
    public void when_trying_to_play_turn_with_wrong_turn_number_then_exception() {
        GameState initialState = state;
        when(initialState.getNextPlayer()).thenReturn(Piece.X);
        Game game = new Game(xPlayer, oPlayer, initialState);

        game.playTurn(new TurnParams(10, null));
    }

}
