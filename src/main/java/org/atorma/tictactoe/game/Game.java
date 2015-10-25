package org.atorma.tictactoe.game;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;
import org.atorma.tictactoe.game.state.Piece;
import org.atorma.tictactoe.game.state.Utils;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class Game {

    private String id = UUID.randomUUID().toString();
    private Map<Piece, Player> players = new EnumMap<>(Piece.class);
    private GameState currentState;
    private Move lastMove;
    private int turnNumber = 1;

    public Game(Player player1, Player player2, GameState initialState) {
        assignPieces(player1, player2);
        Assert.notNull(initialState);

        players.put(player1.getPiece(), player1);
        players.put(player2.getPiece(), player2);

        currentState = initialState.getCopy();
    }

    private void assignPieces(Player player1, Player player2) {
        if (player1.getPiece() == null && player2.getPiece() == null) {
            Piece rndPiece = Piece.values()[Utils.random.nextInt(Piece.values().length)];
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
        return Collections.unmodifiableMap(players);
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public int getTurnNumber() {
        return turnNumber;
    }

    public void playTurn() {
        Piece movePiece = currentState.getTurn();
        Cell moveCell = players.get(movePiece).move(currentState, lastMove != null ? lastMove.getCell() : null);
        currentState = currentState.next(moveCell);
        lastMove = new Move(movePiece, moveCell);
        turnNumber++;
    }


    public static class Move {

        private final Piece piece;
        private final Cell cell;

        public Move(Piece piece, Cell cell) {
            this.piece = piece;
            this.cell = cell;
        }

        public Piece getPiece() {
            return piece;
        }

        public Cell getCell() {
            return cell;
        }
    }
}
