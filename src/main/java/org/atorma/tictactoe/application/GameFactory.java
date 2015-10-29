package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.Game;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameFactory {

    private PlayerRegistry playerRegistry;

    @Autowired
    public GameFactory(PlayerRegistry playerRegistry) {
        this.playerRegistry = playerRegistry;
    }

    public Game createGame(GameParams gameParams) {
        Player player1 = playerRegistry.createPlayer(gameParams.players.player1);
        Player player2 = playerRegistry.createPlayer(gameParams.players.player2);

        Piece[][] board = new Piece[gameParams.board.rows][gameParams.board.columns];

        GameState initialState = new GameState(gameParams.connectHowMany, board, gameParams.firstPlayer);

        Game game = new Game(player1, player2, initialState);

        return game;
    }

}
