package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.state.Piece;

public class GameParams {

    public int connectHowMany = 5;
    public Piece firstPlayer = Piece.X;
    public BoardParams board = new BoardParams();
    public PlayerParams players = new PlayerParams();


    public static class BoardParams {
        public int rows = 18;
        public int columns = 18;
    }

    public static class PlayerParams {
        public PlayerInfo player1;
        public PlayerInfo player2;
    }
}
