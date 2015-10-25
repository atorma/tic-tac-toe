package org.atorma.tictactoe.repository;

import org.atorma.tictactoe.game.Game;

public interface GameRepository {

    Game findById(String id);

    Game save(Game game);

}
