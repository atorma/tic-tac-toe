package org.atorma.tictactoe.game.player;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@Category(FastTests.class)
public class HumanPlayerTests extends UnitTests {

    HumanPlayer humanPlayer;
    @Mock GameState state;
    @Mock Cell opponentsLastMove;

    @Before
    public void setUp() {
        humanPlayer = new HumanPlayer();
    }

    @Test
    public void next_move_is_set_from_outside() {
        Cell nextMove = new Cell(1, 2);
        humanPlayer.setNextMove(nextMove);
        assertThat(humanPlayer.move(state, opponentsLastMove), equalTo(nextMove));
    }

    @Test(expected = IllegalStateException.class)
    public void when_move_attempted_without_setting_next_move_then_exception() {
        humanPlayer.move(state, opponentsLastMove);
    }

    @Test(expected = IllegalStateException.class)
    public void when_next_move_reset_after_move_completed() {
        Cell nextMove = new Cell(1, 2);
        humanPlayer.setNextMove(nextMove);
        humanPlayer.move(state, opponentsLastMove);

        humanPlayer.move(state, opponentsLastMove);
    }
}
