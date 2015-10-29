package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.GameRepository;
import org.atorma.tictactoe.exception.GameNotFoundException;
import org.atorma.tictactoe.game.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryGameRepository.class);

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
        LOGGER.debug("Game {} saved", game.getId());
        return game;
    }

    @Override
    public void delete(Game game) {
        if (game != null) {
            games.remove(game.getId());
            LOGGER.debug("Game {} deleted", game.getId());
        }
    }
}
