package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.state.Piece;

import javax.validation.constraints.*;
import java.util.EnumMap;
import java.util.Map;

public class GameParams {

    @Min(3) public int connectHowMany = 5;
    @NotNull public Piece firstPlayer = Piece.X;
    @NotNull public BoardParams board = new BoardParams();
    @NotNull @Size(min = 2, max = 2) public Map<Piece, PlayerInfo> players = new EnumMap<>(Piece.class);


    public static class BoardParams {
        @Min(3) public int rows = 18;
        @Min(3) public int columns = 18;
    }

}
