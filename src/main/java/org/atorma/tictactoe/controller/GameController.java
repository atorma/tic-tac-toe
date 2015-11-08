package org.atorma.tictactoe.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.atorma.tictactoe.application.GameFactory;
import org.atorma.tictactoe.application.GameParams;
import org.atorma.tictactoe.application.GameRepository;
import org.atorma.tictactoe.exception.GameDeletedException;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/games")
public class GameController {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameController.class);

    private GameRepository gameRepository;
    private GameFactory gameFactory;


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public GameDetailDTO createGame(@RequestBody @Validated GameParams gameParams) {
        Game game = gameFactory.createGame(gameParams);
        game = gameRepository.save(game);
        return new GameDetailDTO(game);
    }

    @RequestMapping(value = "/{gameId}", method = RequestMethod.GET)
    public GameDetailDTO getGame(@PathVariable("gameId") String gameId) {
        return new GameDetailDTO(gameRepository.findById(gameId));
    }

    @RequestMapping(value = "/{gameId}/turns", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public GameDTO playTurn(@PathVariable("gameId") String gameId,
                            @RequestBody TurnRequestDTO turnRequestDTO,
                            HttpServletResponse response) {
        Game game = gameRepository.findById(gameId);

        if (turnRequestDTO.turnNumber != game.getTurnNumber()) {
            throw new TicTacToeException("Trying to play wrong turn");
        }

        game.playTurn();
        try {
            game = gameRepository.save(game);
        } catch (GameDeletedException e) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return null;
        }

        return new GameDTO(game);
    }

    @RequestMapping(value = "/{gameId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable("gameId") String gameId) {
        try {
            Game game = gameRepository.findById(gameId);
            gameRepository.delete(game);
        } catch (NotFoundException e) {
            // OK, it's gone already
        }
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleException(NotFoundException e) {
        LOGGER.warn("Object not found", e);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(TicTacToeException e) {
        LOGGER.warn("Application error (Bad Request)", e);
    }


    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Autowired
    public void setGameFactory(GameFactory gameFactory) {
        this.gameFactory = gameFactory;
    }



    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE)
    public static class GameDTO {

        public final Game game;

        public GameDTO(Game game) {
            this.game = game;
        }

        public String getId() {
            return game.getId();
        }

        public Game.Move getMove() {
            return game.getLastMove();
        }

        public int getTurnNumber() {
            return game.getTurnNumber();
        }

        public Piece getCurrentPlayer() {
            return game.getState().getTurn();
        }

        public boolean isGameEnded() {
            return game.getState().isAtEnd();
        }

        public Piece getWinner() {
            return game.getState().getWinner();
        }

        public GameState.Sequence getWinningSequence() {
            Piece winner = game.getState().getWinner();
            if (winner != null) {
                return game.getState().getLongestSequence(winner);
            } else {
                return null;
            }
        }
    }

    public static class GameDetailDTO extends GameDTO {

        public GameDetailDTO(Game game) {
            super(game);
        }

        public int getConnectHowMany() {
            return game.getState().getConnectHowMany();
        }

        public Piece[][] getBoard() {
            Piece[][] board = new Piece[game.getState().getBoardRows()][game.getState().getBoardCols()];
            for (int i = 0; i < game.getState().getBoardRows(); i++) {
                for (int j = 0; j < game.getState().getBoardCols(); j++) {
                    board[i][j] = game.getState().getPiece(i, j);
                }
            }
            return board;
        }
    }

    public static class TurnRequestDTO {
        public int turnNumber;
    }



}
