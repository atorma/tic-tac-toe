package org.atorma.tictactoe.game;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Game {

    private String id = UUID.randomUUID().toString();
    private Map<Piece, Player> players = new EnumMap<>(Piece.class);
    private GameState currentState;
    private int turnNumber = 1;

    public Game(Player player1, Player player2, GameState initialState) {
        Assert.notNull(player1.getPiece());
        Assert.notNull(player2.getPiece());
        Assert.isTrue(player1.getPiece() != player2.getPiece());
        Assert.notNull(initialState);

        players.put(player1.getPiece(), player1);
        players.put(player2.getPiece(), player2);

        currentState = initialState.getCopy();
    }

    public String getId() {
        return id;
    }

    public Map<Piece, Player> getPlayers() {
        return Collections.unmodifiableMap(players);
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void playTurn() {

    }
}
