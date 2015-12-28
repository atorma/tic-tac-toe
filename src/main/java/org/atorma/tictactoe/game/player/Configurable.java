package org.atorma.tictactoe.game.player;

public interface Configurable {

    /**
     * Called by the game framework before the game begins.
     *
     * @param configuration
     */
    void configure(Object configuration);

}
