package org.atorma.tictactoe.application;

import org.atorma.tictactoe.controller.TurnParams;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.application.ComputationInput;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides a thread-safe access for playing turns and accessing the game state.
 * Thread-safety is NOT guaranteed if you use state-modifying operations of the
 * returned objects.
 */
public class Game {
    private static final Logger LOGGER = LoggerFactory.getLogger(Game.class);

    private final String id = UUID.randomUUID().toString();
    private final Map<Piece, Player> players;
    private final AtomicReference<GameState> state = new AtomicReference<>();
    private final AtomicReference<Move> lastMove = new AtomicReference<>();
    private final AtomicInteger turnNumber = new AtomicInteger(1);
    private final AtomicBoolean deleted = new AtomicBoolean(false);
    private final AtomicReference<ZonedDateTime> timeLastPlayed = new AtomicReference<>();

    public Game(Player player1, Player player2, GameState initialState) {
        Assert.isTrue(player1 != player2, "Players are the same object");
        Assert.notNull(initialState, "Initial state is null");

        assignPieces(player1, player2);

        players = new EnumMap<>(Piece.class);
        players.put(player1.getPiece(), player1);
        players.put(player2.getPiece(), player2);

        state.set(initialState.getCopy());

        timeLastPlayed.set(ZonedDateTime.now());

        LOGGER.debug("New game created. {}: {}, {}: {}", player1.getPiece(), player1, player2.getPiece(), player2);
    }

    private void assignPieces(Player player1, Player player2) {
        if (player1.getPiece() == null && player2.getPiece() == null) {
            Piece rndPiece = Piece.values()[ThreadLocalRandom.current().nextInt(Piece.values().length)];
            player1.setPiece(rndPiece);
            player2.setPiece(rndPiece.other());
        } else if (player1.getPiece() == null && player2.getPiece() != null) {
            player1.setPiece(player2.getPiece().other());
        } else if (player1.getPiece() != null && player2.getPiece() == null) {
            player2.setPiece(player1.getPiece().other());
        } else if (player1.getPiece() == player2.getPiece()) {
            throw new IllegalArgumentException("Both players have piece " + player1.getPiece());
        }
    }

    public String getId() {
        return id;
    }

    public Map<Piece, Player> getPlayers() {
        return players;
    }

    public GameState getState() {
        return state.get();
    }

    public Move getLastMove() {
        return lastMove.get();
    }

    public int getTurnNumber() {
        return turnNumber.get();
    }

    public synchronized void playTurn(TurnParams turnParams, ComputationService computationService) {
        if (turnParams.turnNumber != getTurnNumber()) {
            throw new TicTacToeException("Trying to play wrong turn");
        }

        GameState state = getState();
        Move lastMove = getLastMove();
        Piece piece = state.getNextPlayer();
        Player player = players.get(piece);

        if (player instanceof HumanPlayer) {
            ((HumanPlayer) player).setNextMove(turnParams.getMove());
        }

        var computationInput = new ComputationInput(player, state.getCopy(), lastMove != null ? lastMove.cell() : null);
        var result = computationService.playTurn(computationInput);

        this.state.set(state.next(result.move()));
        this.lastMove.set(new Move(piece, result.move()));
        this.players.put(piece, result.player());

        LOGGER.debug("Turn {}: {} to {}", turnNumber, piece, result.move());
        turnNumber.incrementAndGet();
        timeLastPlayed.set(ZonedDateTime.now());
    }

    public boolean isDeleted() {
        return deleted.get();
    }

    public void setDeleted(boolean deleted) {
        this.deleted.set(deleted);
    }

    public ZonedDateTime getTimeLastPlayed() {
        return timeLastPlayed.get();
    }


    public record Move(Piece piece, Cell cell) {}
}
