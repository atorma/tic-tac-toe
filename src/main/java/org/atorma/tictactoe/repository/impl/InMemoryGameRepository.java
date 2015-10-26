package org.atorma.tictactoe.repository.impl;

import org.atorma.tictactoe.repository.GameRepository;
import org.atorma.tictactoe.exception.GameNotFoundException;
import org.atorma.tictactoe.game.Game;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();

    @Override
    public Game findById(String id) {
        Game game = games.get(id);
        if (game == null) {
            throw new GameNotFoundException("Game id " + id + " not found");
        } else {
            return game;
        }
    }

    @Override
    public Game save(Game game) {
        Assert.notNull(game);
        Assert.notNull(game.getId());
        games.put(game.getId(), game);
        return game;
    }

    @Override
    public void delete(Game game) {
        if (game != null) {
            games.remove(game.getId());
        }
    }
}
