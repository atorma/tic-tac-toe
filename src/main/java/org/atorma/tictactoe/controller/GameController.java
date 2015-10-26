package org.atorma.tictactoe.controller;

import org.atorma.tictactoe.exception.GameNotFoundException;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.EnumMap;
import java.util.Map;

@RestController
@RequestMapping("/games")
public class GameController {

    private GameRepository gameRepository;

    @RequestMapping(value = "", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public GameDTO createGame() {
        NaivePlayer naivePlayer = new NaivePlayer();
        MCTSPlayer mctsPlayer = new MCTSPlayer();
        GameState initialState = new GameState(5, new Piece[18][18], Piece.X);
        Game game = new Game(naivePlayer, mctsPlayer, initialState);
        game = gameRepository.save(game);
        return new GameDTO(game);
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
    public void gameNotFound(GameNotFoundException e) {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void gameNotFound(TicTacToeException e) {
    }


    @Autowired
    public void setGameRepository(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }


    public static class GameDTO {

        private final Game game;
        private final Map<Piece, PlayerDTO> players;

        public GameDTO(Game game) {
            this.game = game;
            this.players = new EnumMap<>(Piece.class);
            for (Piece p : Piece.values()) {
                players.put(p, new PlayerDTO(game.getPlayers().get(p)));
            }
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
            return game.getCurrentState().getTurn();
        }

        public boolean isGameEnded() {
            return game.getCurrentState().isAtEnd();
        }

        public Piece getWinner() {
            return game.getCurrentState().getWinner();
        }

        public GameState.Sequence getWinningSequence() {
            Piece winner = game.getCurrentState().getWinner();
            if (winner != null) {
                return game.getCurrentState().getLongestSequence(winner);
            } else {
                return null;
            }
        }

        public Map<Piece, PlayerDTO> getPlayers() {
            return players;
        }

    }

    public static class PlayerDTO {

        private final Player player;

        public PlayerDTO(Player player) {
            this.player = player;
        }

        public String getName() {
            return player.getName();
        }
    }

    public static class TurnRequestDTO {
        public int turnNumber;
    }



}
