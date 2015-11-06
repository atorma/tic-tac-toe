package org.atorma.tictactoe.exception;

public class GameDeletedException extends TicTacToeException {

    public GameDeletedException() {
    }

    public GameDeletedException(String message) {
        super(message);
    }

    public GameDeletedException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameDeletedException(Throwable cause) {
        super(cause);
    }

    public GameDeletedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
