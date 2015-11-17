package org.atorma.tictactoe.controller;

import com.jayway.jsonpath.JsonPath;
import org.atorma.tictactoe.ApplicationMvcTests;
import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.application.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.application.GameRepository;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Category(FastTests.class)
public class GameRestApplicationTests extends ApplicationMvcTests {

    @Autowired GameRepository gameRepository;
    @Autowired PlayerRegistry playerRegistry;

    Game aiVsAiGame;
    Game humanVsAiGame;

    @Before
    public void setUp() {
        setUpAiVsAiGame();
        setUpHumanVsAiGame();
    }

    private void setUpAiVsAiGame() {
        Player player1 = new NaivePlayer();
        Player player2 = new NaivePlayer();
        GameState initialState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.O).build();
        Game game = new Game(player1, player2, initialState);
        aiVsAiGame = gameRepository.save(game);
    }

    private void setUpHumanVsAiGame() {
        Player player1 = new HumanPlayer();
        player1.setPiece(Piece.X);
        Player player2 = new NaivePlayer();
        player2.setPiece(Piece.O);
        GameState initialState = GameState.builder().setConnectHowMany(3).setBoard(new Piece[3][3]).setNextPlayer(Piece.X).build(); // HumanPlayer starts
        Game game = new Game(player1, player2, initialState);
        humanVsAiGame = gameRepository.save(game);
    }

    @Test
    public void create_game() throws Exception {
        String gameOptionsJson =
                JsonBuilderFactory.buildObject()
                    .add("connectHowMany", 3)
                    .add("firstPlayer", Piece.O.toString())
                    .addObject("board")
                        .add("rows", 5)
                        .add("columns", 6)
                    .end()
                    .addObject("players")
                        .addObject(Piece.X.toString())
                            .add("id", playerRegistry.getPlayerInfoList().get(1).getId())
                        .end()
                        .addObject(Piece.O.toString())
                            .add("id", playerRegistry.getPlayerInfoList().get(2).getId())
                        .end()
                    .end()
                .end()
                .toString();

        ResultActions resultActions = mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameOptionsJson))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.turnNumber").value(1))
                .andExpect(jsonPath("$.nextPlayer").value(Piece.O.toString()))
                .andExpect(jsonPath("$.connectHowMany").value(3))
                .andExpect(jsonPath("$.board").value(hasSize(5)))
                .andExpect(jsonPath("$.board[0]").value(hasSize(6)))
                .andExpect(jsonPath("$.move").value(nullValue()))
                .andExpect(jsonPath("$.gameEnded").value(false))
                .andExpect(jsonPath("$.winner").value(nullValue()))
                .andExpect(jsonPath("$.winningSequence").value(nullValue()))
                ;

        String gameId = JsonPath.read(resultActions.andReturn().getResponse().getContentAsString(), "$.id");
        Game createdGame = gameRepository.findById(gameId);
        assertThat(createdGame.getId(), equalTo(gameId));
    }

    @Test
    public void status_bad_request_when_trying_to_create_game_with_bad_options() throws Exception {
        String invalidOptionsJson;

        invalidOptionsJson = JsonBuilderFactory.buildObject()
                        .add("connectHowMany", 1) // !
                        .add("firstPlayer", Piece.O.toString())
                        .addObject("board")
                            .add("rows", 5)
                            .add("columns", 6)
                        .end()
                        .addObject("players")
                            .addObject(Piece.X.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(1).getId())
                            .end()
                            .addObject(Piece.O.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(2).getId())
                            .end()
                        .end()
                    .end()
                    .toString();

        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOptionsJson))
                .andExpect(status().isBadRequest());


        invalidOptionsJson = JsonBuilderFactory.buildObject()
                        .add("connectHowMany", 3)
                        .add("firstPlayer", Piece.O.toString())
                        .addObject("board")
                            .add("rows", 2) // !
                            .add("columns", 6)
                        .end()
                        .addObject("players")
                            .addObject(Piece.X.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(1).getId())
                            .end()
                            .addObject(Piece.O.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(2).getId())
                            .end()
                        .end()
                    .end()
                    .toString();

        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOptionsJson))
                .andExpect(status().isBadRequest());

        invalidOptionsJson = JsonBuilderFactory.buildObject()
                        .add("connectHowMany", 5)
                        .add("firstPlayer", (String) null) // !
                        .addObject("board")
                            .add("rows", 18)
                            .add("columns", 18)
                        .end()
                        .addObject("players")
                            .addObject(Piece.X.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(1).getId())
                            .end()
                            .addObject(Piece.O.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(2).getId())
                            .end()
                        .end()
                    .end()
                    .toString();

        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOptionsJson))
                .andExpect(status().isBadRequest());

        invalidOptionsJson = JsonBuilderFactory.buildObject()
                        .add("connectHowMany", 5)
                        .add("firstPlayer", Piece.O.toString())
                        .addObject("board")
                            .add("rows", 18)
                            .add("columns", 18)
                        .end()
                        .addObject("players")
                            .addObject(Piece.O.toString())
                                .add("id", playerRegistry.getPlayerInfoList().get(1).getId())
                            .end()
                            .add(Piece.X.toString(), (String) null) // !
                        .end()
                    .end()
                    .toString();

        mockMvc.perform(post("/games")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidOptionsJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void get_game_state() throws Exception {

        aiVsAiGame.playTurn(new TurnParams(aiVsAiGame.getTurnNumber(), null));
        Game.Move lastMove = aiVsAiGame.getLastMove();
        Piece winner = aiVsAiGame.getState().getWinner();

        mockMvc.perform(get("/games/{id}", aiVsAiGame.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move.piece").value(lastMove.getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(lastMove.getCell().getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(lastMove.getCell().getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(aiVsAiGame.getTurnNumber()))
                .andExpect(jsonPath("$.nextPlayer").value(aiVsAiGame.getState().getNextPlayer().toString()))
                .andExpect(jsonPath("$.gameEnded").value(aiVsAiGame.getState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(winner != null ? winner.toString() : null))
                .andExpect(jsonPath("$.winningSequence").value(winner != null ? notNullValue() : nullValue()))
                .andExpect(jsonPath("$.connectHowMany").value(aiVsAiGame.getState().getConnectHowMany()))
                .andExpect(jsonPath("$.board[" + lastMove.getCell().getRow() + "][" + lastMove.getCell().getColumn() + "]").value(lastMove.getPiece().toString()))
        ;
    }

    @Test
    public void play_ai_turn() throws Exception {
        String turnJson = JsonBuilderFactory.buildObject()
                    .add("turnNumber", aiVsAiGame.getTurnNumber())
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", aiVsAiGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move.piece").value(aiVsAiGame.getLastMove().getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(aiVsAiGame.getLastMove().getCell().getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(aiVsAiGame.getLastMove().getCell().getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(aiVsAiGame.getTurnNumber()))
                .andExpect(jsonPath("$.nextPlayer").value(aiVsAiGame.getState().getNextPlayer().toString()))
                .andExpect(jsonPath("$.gameEnded").value(aiVsAiGame.getState().isAtEnd()))
                .andExpect(jsonPath("$.winner").value(aiVsAiGame.getState().getWinner() != null ? aiVsAiGame.getState().getWinner().toString() : null))
                ;
    }

    @Test
    public void play_human_turn() throws Exception {
        Cell humanPlayerMove = new Cell(1, 1);

        String turnJson = JsonBuilderFactory.buildObject()
                    .add("turnNumber", humanVsAiGame.getTurnNumber())
                    .addObject("move")
                        .add("row", humanPlayerMove.getRow())
                        .add("column", humanPlayerMove.getColumn())
                .end()
                .toString();

        mockMvc.perform(post("/games/{id}/turns", humanVsAiGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move.piece").value(humanVsAiGame.getLastMove().getPiece().toString()))
                .andExpect(jsonPath("$.move.cell.row").value(humanPlayerMove.getRow()))
                .andExpect(jsonPath("$.move.cell.column").value(humanPlayerMove.getColumn()))
                .andExpect(jsonPath("$.turnNumber").value(humanVsAiGame.getTurnNumber()))
                .andExpect(jsonPath("$.nextPlayer").value(humanVsAiGame.getState().getNextPlayer().toString()))
                .andExpect(jsonPath("$.gameEnded").value(humanVsAiGame.getState().isAtEnd()))
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

        mockMvc.perform(post("/games/{id}/turns", aiVsAiGame.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(turnJson))
                .andExpect(status().isBadRequest());
    }

    @Test(expected = NotFoundException.class)
    public void delete_game() throws Exception {
        mockMvc.perform(delete("/games/{id}", aiVsAiGame.getId()))
                .andExpect(status().isNoContent());

        gameRepository.findById(aiVsAiGame.getId());
    }
}
