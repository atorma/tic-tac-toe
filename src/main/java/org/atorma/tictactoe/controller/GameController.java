package org.atorma.tictactoe.controller;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.atorma.tictactoe.exception.GameNotFoundException;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.application.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/games")
public class GameController {

    private GameRepository gameRepository;

    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public GameDetailDTO createGame() {
        NaivePlayer naivePlayer = new NaivePlayer();
        MCTSPlayer mctsPlayer = new MCTSPlayer();
        GameState initialState = new GameState(5, new Piece[18][18], Piece.X);
        Game game = new Game(naivePlayer, mctsPlayer, initialState);
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
                            @RequestBody TurnRequestDTO turnRequestDTO) {
        Game game = gameRepository.findById(gameId);

        if (turnRequestDTO.turnNumber != game.getTurnNumber()) {
            throw new TicTacToeException("Trying to play wrong turn");
        }

        game.playTurn();
        game = gameRepository.save(game);

        return new GameDTO(game);
    }

    @RequestMapping(value = "/{gameId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGame(@PathVariable("gameId") String gameId) {
        try {
            Game game = gameRepository.findById(gameId);
            gameRepository.delete(game);
        } catch (GameNotFoundException e) {
            // OK, it's gone already
        }
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleException(GameNotFoundException e) {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleException(TicTacToeException e) {
    }


    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
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
