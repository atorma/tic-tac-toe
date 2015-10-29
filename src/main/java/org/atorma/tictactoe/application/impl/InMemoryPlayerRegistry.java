package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class InMemoryPlayerRegistry implements PlayerRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryPlayerRegistry.class);

    private List<PlayerInfo> playerInfoList = new ArrayList<>();
    private Map<String, Class<? extends Player>> playerClasses = new HashMap<>();


    public InMemoryPlayerRegistry() {
        createPlayer("Monte Carlo Tree Search", MCTSPlayer.class);
        createPlayer("Naive", NaivePlayer.class);
        createPlayer("Random", RandomPlayer.class);
    }

    private void createPlayer(String name, Class<? extends Player> playerClass) {
        PlayerInfo playerInfo = new PlayerInfo(UUID.randomUUID().toString(), name);
        playerInfoList.add(playerInfo);
        playerClasses.put(playerInfo.getId(), playerClass);
    }


    @Override
    public List<PlayerInfo> getPlayerInformation() {
        return new ArrayList<>(playerInfoList);
    }

    @Override
    public Player createPlayer(PlayerInfo playerInfo) {
        Class<? extends Player> playerClass = playerClasses.get(playerInfo.getId());
        if (playerClass == null) {
            throw new TicTacToeException("Cannot find player id = " + playerInfo.getId() + ", name = " + playerInfo.getName());
        }
        try {
            Player player = playerClass.newInstance();
            // TODO configure using input
            return player;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TicTacToeException("Error when creating player", e);
        }
    }
}
