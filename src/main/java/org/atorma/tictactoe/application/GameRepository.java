package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.Game;

public interface GameRepository {

    Game findById(String id);

    Game save(Game game);

    void delete(Game game);
}
