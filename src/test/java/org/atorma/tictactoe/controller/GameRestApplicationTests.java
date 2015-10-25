package org.atorma.tictactoe.controller;

import com.jayway.jsonpath.JsonPath;
import org.atorma.tictactoe.ApplicationMvcTests;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.repository.GameRepository;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class GameRestApplicationTests extends ApplicationMvcTests {

    @Autowired private GameRepository gameRepository;

    @Test
    public void create_game() throws Exception {
        String gameOptionsJson = JsonBuilderFactory.buildObject()
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
                .andExpect(jsonPath("$.currentPlayer").value(createdGame.getCurrentState().getTurn().toString()))
                .andExpect(jsonPath("$.gameEnded").value(createdGame.getCurrentState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(createdGame.getCurrentState().getWinner()))
                .andExpect(jsonPath("$.players.X.name").value(createdGame.getPlayers().get(Piece.X).toString()))
                .andExpect(jsonPath("$.players.O.name").value(createdGame.getPlayers().get(Piece.O).toString()));
    }

    @Test
    public void play_turn() throws Exception {
        String initialResponse = mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andReturn().getResponse().getContentAsString();
        String gameId = JsonPath.read(initialResponse, "$.id");
        Game game = gameRepository.findById(gameId);

        String turnJson = JsonBuilderFactory.buildObject()
                .add("turnNumber", game.getTurnNumber())
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", game.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.move.piece").value(game.getLastMove().getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(game.getLastMove().getCell().getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(game.getLastMove().getCell().getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(game.getTurnNumber()))
                .andExpect(jsonPath("$.currentPlayer").value(game.getCurrentState().getTurn().toString()))
                .andExpect(jsonPath("$.gameEnded").value(game.getCurrentState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(nullValue()))
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
        String initialResponse = mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andReturn().getResponse().getContentAsString();
        String gameId = JsonPath.read(initialResponse, "$.id");

        String turnJson = JsonBuilderFactory.buildObject()
                .add("turnNumber", 999)
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", gameId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andExpect(status().isBadRequest());
    }

}
