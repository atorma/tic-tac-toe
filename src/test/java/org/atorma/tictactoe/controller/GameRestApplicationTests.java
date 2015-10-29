package org.atorma.tictactoe.controller;

import com.jayway.jsonpath.JsonPath;
import org.atorma.tictactoe.ApplicationMvcTests;
import org.atorma.tictactoe.exception.GameNotFoundException;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.application.GameRepository;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameRestApplicationTests extends ApplicationMvcTests {

    @Autowired GameRepository gameRepository;

    Game existingGame;

    @Before
    public void setUp() {
        Player player1 = new NaivePlayer();
        Player player2 = new NaivePlayer();
        GameState initialState = new GameState(3, new Piece[3][3], Piece.O);
        Game game = new Game(player1, player2, initialState);
        existingGame = gameRepository.save(game);
    }


    @Test
    public void create_game() throws Exception {
        String gameOptionsJson =
                JsonBuilderFactory.buildObject()
                    .addObject("board")
                        .add("rows", 5)
                        .add("columns", 6)
                    .end()
                    .addObject("players")
                        .addObject(Piece.X.toString())
                            .add("id", 1)
                        .end()
                        .addObject(Piece.O.toString())
                            .add("id", 1)
                        .end()
                    .end()
                .end()
                .toString();

        ResultActions resultActions = mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameOptionsJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                ;

        String gameId = JsonPath.read(resultActions.andReturn().getResponse().getContentAsString(), "$.id");
        Game createdGame = gameRepository.findById(gameId);
        assertThat(createdGame.getId(), equalTo(gameId));

        resultActions
                .andExpect(jsonPath("$.turnNumber").value(createdGame.getTurnNumber()))
                .andExpect(jsonPath("$.currentPlayer").value(createdGame.getState().getTurn().toString()))
                .andExpect(jsonPath("$.gameEnded").value(createdGame.getState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(createdGame.getState().getWinner()))
                .andExpect(jsonPath("$.players.X.name").value(createdGame.getPlayers().get(Piece.X).toString()))
                .andExpect(jsonPath("$.players.O.name").value(createdGame.getPlayers().get(Piece.O).toString()));
    }

    @Test
    public void get_game_state() throws Exception {

        existingGame.playTurn();
        Game.Move lastMove = existingGame.getLastMove();
        Piece winner = existingGame.getState().getWinner();

        mockMvc.perform(get("/games/{id}", existingGame.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move.piece").value(lastMove.getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(lastMove.getCell().getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(lastMove.getCell().getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(existingGame.getTurnNumber()))
                .andExpect(jsonPath("$.currentPlayer").value(existingGame.getState().getTurn().toString()))
                .andExpect(jsonPath("$.gameEnded").value(existingGame.getState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(winner != null ? winner.toString() : null))
                .andExpect(jsonPath("$.winningSequence").value(winner != null ? notNullValue() : nullValue()))
                .andExpect(jsonPath("$.board[" + lastMove.getCell().getRow() + "][" + lastMove.getCell().getColumn() + "]").value(lastMove.getPiece().toString()))
        ;
    }



    @Test
    public void play_turn() throws Exception {
        String turnJson = JsonBuilderFactory.buildObject()
                .add("turnNumber", existingGame.getTurnNumber())
                .end()
                .toString();

        Piece winner = existingGame.getState().getWinner();
        mockMvc.perform(post("/games/{id}/turns", existingGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.move.piece").value(existingGame.getLastMove().getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(existingGame.getLastMove().getCell().getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(existingGame.getLastMove().getCell().getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(existingGame.getTurnNumber()))
                .andExpect(jsonPath("$.currentPlayer").value(existingGame.getState().getTurn().toString()))
                .andExpect(jsonPath("$.gameEnded").value(existingGame.getState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(winner != null ? winner.toString() : null))
                .andExpect(jsonPath("$.winningSequence").value(winner != null ? notNullValue() : nullValue()))
                ;
    }


    @Test
    public void http_not_found_if_game_not_found_by_id() throws Exception {
        String turnJson = JsonBuilderFactory.buildObject()
                .add("turnNumber", 1)
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", "nonexisting")
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andExpect(status().isNotFound());
    }

    @Test
    public void http_bad_request_if_requesting_to_play_turn_with_wrong_number() throws Exception {
        String turnJson = JsonBuilderFactory.buildObject()
                .add("turnNumber", 999)
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", existingGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = GameNotFoundException.class)
    public void delete_game() throws Exception {
        mockMvc.perform(delete("/games/{id}", existingGame.getId()))
                .andExpect(status().isNoContent());

        gameRepository.findById(existingGame.getId());
    }
}
