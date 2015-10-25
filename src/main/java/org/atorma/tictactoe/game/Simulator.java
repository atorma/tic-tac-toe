package org.atorma.tictactoe.game;

import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.state.Cell;
import org.atorma.tictactoe.game.state.GameState;

public class Simulator {

    private final GameState startingState;
    private final Player player1;
    private final Player player2;
    private boolean copyBoard = true;

    public Simulator(GameState startingState, Player player1, Player player2) {
        this.startingState = startingState;
        this.player1 = player1;
        this.player2 = player2;
    }

    /**
     * Simulate a game until it ends.
     *
     * @return
     *  the end state
     */
    public GameState run() {
        return run(Long.MAX_VALUE, Integer.MAX_VALUE);
    }

    /**
     * Simulate a game until time or number of turns to play runs out.
     *
     * @param maxTimeMillis
     *  Maximum time in milliseconds to simulate
     * @param  maxTurns
     *  Maximum number of turns to play the game
     * @return
     *  the end state
     */
    public GameState run(long maxTimeMillis, int maxTurns) {
        int turns = 0;
        long startTime = System.currentTimeMillis();

        GameState gameState = startingState.getCopy();
        Cell lastPosition = null;

        Player currentPlayer;
        if (startingState.getTurn() == player1.getPiece()) {
            currentPlayer = player1;
        } else {
            currentPlayer = player2;
        }

        while (!gameState.isAtEnd()
                && (System.currentTimeMillis() - startTime) < maxTimeMillis
                && turns < maxTurns) {

            GameState inputState = copyBoard ? gameState.getCopy() : gameState;
            Cell nextPosition = currentPlayer.move(inputState, lastPosition);

            if (currentPlayer == player1) {
                currentPlayer = player2;
            } else {
                currentPlayer = player1;
            }

            gameState.update(nextPosition);
            lastPosition = nextPosition;
            turns++;
        }

        return gameState;
    }

    public boolean isCopyBoard() {
        return copyBoard;
    }

    public void setCopyBoard(boolean copyBoard) {
        this.copyBoard = copyBoard;
    }
}
