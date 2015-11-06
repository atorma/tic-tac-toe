package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.UnitTests;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.game.Game;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InMemoryGameRepositoryTests extends UnitTests {

    private InMemoryGameRepository repository;

    @Before
    public void setUp() {
        repository = new InMemoryGameRepository();
    }

    @Test
    public void after_saving_game_is_found_by_id() {
        Game game = mock(Game.class);
        when(game.getId()).thenReturn(UUID.randomUUID().toString());

        Game savedGame = repository.save(game);

        assertThat(savedGame, is(game));

        Game foundGame = repository.findById(game.getId());

        assertThat(foundGame, is(game));
    }

    @Test(expected = NotFoundException.class)
    public void after_deleting_game_not_found_by_id() {
        Game game = mock(Game.class);
        when(game.getId()).thenReturn(UUID.randomUUID().toString());

        Game savedGame = repository.save(game);

        repository.delete(savedGame);

        repository.findById(game.getId());
    }

    @Test(expected = NotFoundException.class)
    public void exception_when_no_game_with_given_id() {
        repository.findById("aargh");
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_when_saving_null_game() {
        repository.save(null);
    }
}
