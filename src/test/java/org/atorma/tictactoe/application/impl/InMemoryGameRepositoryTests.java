package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.exception.NotFoundException;import org.atorma.tictactoe.exception.GameDeletedException;
import org.atorma.tictactoe.application.Game;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mock;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(FastTests.class)
public class InMemoryGameRepositoryTests extends UnitTests {

    InMemoryGameRepository repository;
    @Mock Game game;


    @Before
    public void setUp() {
        repository = new InMemoryGameRepository();
        when(game.getId()).thenReturn(UUID.randomUUID().toString());
    }

    @Test
    public void after_saving_game_is_found_by_id() {
        Game savedGame = repository.save(game);

        assertThat(savedGame, is(game));

        Game foundGame = repository.findById(game.getId());

        assertThat(foundGame, is(game));
    }

    @Test(expected = NotFoundException.class)
    public void exception_when_no_game_with_given_id() {
        repository.findById("aargh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_when_saving_null_game() {
        repository.save(null);
    }


    @Test(expected = NotFoundException.class)
    public void after_deleting_saved_game_not_found_by_id() {
        Game game = mock(Game.class);
        when(game.getId()).thenReturn(UUID.randomUUID().toString());

        Game savedGame = repository.save(game);

        repository.delete(savedGame);

        repository.findById(game.getId());
    }

    // Due to concurrency a game in memory may be saved after deleting from repository.
    // We don't want a deleted game to be restored.

    @Test
    public void when_deleted_then_game_is_marked_as_deleted() {
        repository.delete(game);

        verify(game).setDeleted(true);
    }

    @Test(expected = GameDeletedException.class)
    public void when_game_is_flagged_as_deleted_then_it_will_not_be_saved() {
        when(game.isDeleted()).thenReturn(true);

        repository.save(game);
    }


}
