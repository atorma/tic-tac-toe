package org.atorma.tictactoe.application;

public interface GameRepository {

    Game findById(String id);

    Game save(Game game);

    void delete(Game game);
}
