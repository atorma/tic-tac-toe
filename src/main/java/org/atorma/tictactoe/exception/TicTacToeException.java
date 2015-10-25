package org.atorma.tictactoe.exception;

public class TicTacToeException extends RuntimeException {

    public TicTacToeException() {
    }

    public TicTacToeException(String message) {
        super(message);
    }

    public TicTacToeException(String message, Throwable cause) {
        super(message, cause);
    }

    public TicTacToeException(Throwable cause) {
        super(cause);
    }

    public TicTacToeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
