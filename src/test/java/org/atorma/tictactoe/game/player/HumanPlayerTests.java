package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("FastTests")
public class HumanPlayerTests extends UnitTests {

    HumanPlayer humanPlayer;
    @Mock GameState state;
    @Mock Cell opponentsLastMove;

    @BeforeEach
    public void setUp() {
        humanPlayer = new HumanPlayer();
    }

    @Test
    public void next_move_is_set_from_outside() {
        Cell nextMove = new Cell(1, 2);
        humanPlayer.setNextMove(nextMove);
        assertThat(humanPlayer.move(state, opponentsLastMove), equalTo(nextMove));
    }

    @Test
    public void when_move_attempted_without_setting_next_move_then_exception() {
        assertThrows(IllegalStateException.class, () -> humanPlayer.move(state, opponentsLastMove));
    }

    @Test
    public void when_next_move_reset_after_move_completed() {
        Cell nextMove = new Cell(1, 2);
        humanPlayer.setNextMove(nextMove);
        humanPlayer.move(state, opponentsLastMove);

        assertThrows(IllegalStateException.class, () -> humanPlayer.move(state, opponentsLastMove));
    }
}
