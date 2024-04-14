package org.atorma.tictactoe.application;

import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.controller.TurnParams;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("FastTests")
public class GameTests extends UnitTests {

    @Mock private Player xPlayer;
    @Mock private Player oPlayer;
    @Mock private GameState state;


    @BeforeEach
    public void setUp() {
        lenient().when(xPlayer.getPiece()).thenReturn(Piece.X);
        lenient().when(oPlayer.getPiece()).thenReturn(Piece.O);

        lenient().when(state.getCopy()).thenReturn(state);
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
        when(initialState.getCopy()).thenReturn(initialState);
        Game game = new Game(xPlayer, oPlayer, initialState);

        Cell xPlayerMove = new Cell(5, 5);
        when(xPlayer.move(initialState, null)).thenReturn(xPlayerMove);
        GameState afterXPlayerMove = mock(GameState.class);
        when(initialState.next(xPlayerMove)).thenReturn(afterXPlayerMove);
        when(afterXPlayerMove.getNextPlayer()).thenReturn(Piece.O);
        when(afterXPlayerMove.getCopy()).thenReturn(afterXPlayerMove);

        game.playTurn(new TurnParams(1, new Cell(1, 1)));

        assertEquals(afterXPlayerMove, game.getState());
        assertEquals(xPlayerMove, game.getLastMove().getCell());
        assertEquals(2, game.getTurnNumber());

        Cell oPlayerMove = new Cell(6, 6);
        when(oPlayer.move(afterXPlayerMove, xPlayerMove)).thenReturn(oPlayerMove);
        GameState afterOPlayerMove = mock(GameState.class);
        when(afterXPlayerMove.next(oPlayerMove)).thenReturn(afterOPlayerMove);

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

        game.playTurn(new TurnParams(1, humanPlayerMove));

        verify(humanPlayer).setNextMove(humanPlayerMove);
    }

    @Test
    public void when_trying_to_play_turn_with_wrong_turn_number_then_exception() {
        GameState initialState = state;
        Game game = new Game(xPlayer, oPlayer, initialState);

        assertThrows(TicTacToeException.class, () -> game.playTurn(new TurnParams(10, null)));
    }

    @Test
    public void when_turn_played_then_last_played_timestamp_updated() {
        Game game = new Game(xPlayer, oPlayer, state);
        when(state.getNextPlayer()).thenReturn(Piece.X);
        when(xPlayer.move(any(GameState.class), any())).thenReturn(new Cell(2, 2));

        ZonedDateTime beforePlay = game.getTimeLastPlayed();

        game.playTurn(new TurnParams(1, new Cell(1, 1)));

        ZonedDateTime afterPlay = game.getTimeLastPlayed();

        assertTrue(afterPlay.isAfter(beforePlay));
    }
}
