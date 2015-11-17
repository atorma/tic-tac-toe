package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameFactory {

    private PlayerRegistry playerRegistry;

    @Autowired
    public GameFactory(PlayerRegistry playerRegistry) {
        this.playerRegistry = playerRegistry;
    }

    public Game createGame(GameParams gameParams) {
        Assert.isTrue(gameParams.players.size() == 2, "Must have 2 players");
        Assert.isTrue(!gameParams.players.keySet().contains(null), "Null piece assignment not allowed");

        Piece[][] board = new Piece[gameParams.board.rows][gameParams.board.columns];

        GameState initialState = GameState.builder().setConnectHowMany(gameParams.connectHowMany).setBoard(board).setNextPlayer(gameParams.firstPlayer).build();

        List<Player> players = new ArrayList<>(2);
        for (Piece piece : Piece.values()) {
            Player player = playerRegistry.createPlayer(gameParams.players.get(piece));
            player.setPiece(piece);
            players.add(player);
        }

        Game game = new Game(players.get(0), players.get(1), initialState);

        return game;
    }

}
