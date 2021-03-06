package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.GameRepository;
import org.atorma.tictactoe.exception.GameDeletedException;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.application.Game;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryGameRepository implements GameRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryGameRepository.class);

    private ConcurrentHashMap<String, Game> games = new ConcurrentHashMap<>();
    private long autoDeletePeriodSeconds;


    @Override
    public Game findById(String id) {
        Game game = games.get(id);
        if (game == null) {
            throw new NotFoundException("Game id " + id + " not found");
        } else {
            return game;
        }
    }

    @Override
    public Game save(Game game) {
        Assert.notNull(game);
        Assert.notNull(game.getId());
        if (game.isDeleted()) {
            throw new GameDeletedException("Game " + game.getId() + " is deleted");
        }

        games.put(game.getId(), game);
        LOGGER.debug("Game {} saved", game.getId());
        return game;
    }

    @Override
    public void delete(Game game) {
        if (game != null) {
            game.setDeleted(true);
            games.remove(game.getId());
            LOGGER.debug("Game {} deleted", game.getId());
        }
    }

    @Scheduled(fixedDelay = 60000)
    public void deleteInactiveGames() {
        ZonedDateTime now = ZonedDateTime.now();
        games.values().stream()
                .filter(x -> Duration.between(x.getTimeLastPlayed(), now).getSeconds() > autoDeletePeriodSeconds)
                .forEach(this::delete);

    }

    @Value("${game.autoDeletePeriodMinutes}")
    public void setAutoDeletePeriodMinutes(int autoDeletePeriodMinutes) {
        this.autoDeletePeriodSeconds = autoDeletePeriodMinutes*60;
    }
}
