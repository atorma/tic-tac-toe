package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.exception.TicTacToeException;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.player.mcts.MCTSPlayer;
import org.atorma.tictactoe.game.player.naive.NaivePlayer;
import org.atorma.tictactoe.game.player.random.RandomAdjacentPlayer;
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
        createPlayer("Human", PlayerInfo.Type.HUMAN, HumanPlayer.class);
        createPlayer("Monte Carlo Tree Search", PlayerInfo.Type.AI, MCTSPlayer.class);
        createPlayer("Naive", PlayerInfo.Type.AI, NaivePlayer.class);
        createPlayer("Random adjacent", PlayerInfo.Type.AI, RandomAdjacentPlayer.class);
        createPlayer("Uniform random", PlayerInfo.Type.AI, RandomPlayer.class);
    }

    private void createPlayer(String name, PlayerInfo.Type type, Class<? extends Player> playerClass) {
        PlayerInfo playerInfo = new PlayerInfo(UUID.randomUUID().toString(), name, type);
        playerInfoList.add(playerInfo);
        playerClasses.put(playerInfo.getId(), playerClass);
    }


    @Override
    public List<PlayerInfo> getPlayerInfoList() {
        return new ArrayList<>(playerInfoList);
    }

    @Override
    public PlayerInfo getPlayerInfoById(String id) {
        PlayerInfo playerInfo = playerInfoList.stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        if (playerInfo != null) {
            return playerInfo;
        } else {
            throw new NotFoundException("Could not find player info with id = " + id);
        }
    }

    @Override
    public Player createPlayer(PlayerInfo playerInfo) {
        Class<? extends Player> playerClass = playerClasses.get(playerInfo.getId());
        if (playerClass == null) {
            throw new NotFoundException("Cannot find player id = " + playerInfo.getId() + ", name = " + playerInfo.getName());
        }
        try {
            Player player = playerClass.newInstance();
            // TODO configure using input
            return player;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error when creating player", e);
        }
    }
}
