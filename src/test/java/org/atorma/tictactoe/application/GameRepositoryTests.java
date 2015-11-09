package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class GameRepositoryTests extends ApplicationTests {

    @Autowired private GameRepository gameRepository;

    @Test
    public void save_and_find() {
        GameState initialState = new GameState(3, new Piece[3][3], Piece.O);
        Game game = new Game(new NaivePlayer(), new NaivePlayer(), initialState);
        game = gameRepository.save(game);
        assertThat(gameRepository.findById(game.getId()), equalTo(game));
    }

}
