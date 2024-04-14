package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Tag("FastTests")
public class GameRepositoryTests extends ApplicationTests {

    @Autowired private GameRepository gameRepository;

    @Test
    public void save_and_find() {
        GameState initialState = GameState.builder()
                .setConnectHowMany(3)
                .setBoard(new Piece[3][3])
                .setNextPlayer(Piece.O)
                .build();
        Game game = new Game(new NaivePlayer(), new NaivePlayer(), initialState);
        game = gameRepository.save(game);
        assertThat(gameRepository.findById(game.getId()), equalTo(game));
    }

}
