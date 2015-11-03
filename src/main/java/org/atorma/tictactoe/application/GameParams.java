package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.state.Piece;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.EnumMap;
import java.util.Map;

public class GameParams {

    @Min(3) public int connectHowMany = 5;
    @NotNull public Piece firstPlayer = Piece.X;
    @NotNull @Valid public BoardParams board = new BoardParams();
    public Map<Piece, PlayerInfo> players = new EnumMap<>(Piece.class);

    public int getConnectHowMany() {
        return connectHowMany;
    }

    public void setConnectHowMany(int connectHowMany) {
        this.connectHowMany = connectHowMany;
    }

    public Piece getFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(Piece firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public BoardParams getBoard() {
        return board;
    }

    public void setBoard(BoardParams board) {
        this.board = board;
    }

    public Map<Piece, PlayerInfo> getPlayers() {
        return players;
    }

    public void setPlayers(Map<Piece, PlayerInfo> players) {
        this.players = players;
    }

    @AssertTrue
    public boolean isPlayersValid() {
        return players != null && players.size() == 2
                && !players.containsKey(null) && !players.containsValue(null);
    }

    public static class BoardParams {
        @Min(3) public int rows = 18;
        @Min(3) public int columns = 18;

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }
    }

}
