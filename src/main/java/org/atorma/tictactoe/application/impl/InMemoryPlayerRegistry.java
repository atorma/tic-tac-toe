package org.atorma.tictactoe.application.impl;

import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.atorma.tictactoe.exception.NotFoundException;
import org.atorma.tictactoe.game.player.Configurable;
import org.atorma.tictactoe.game.player.Player;
import org.atorma.tictactoe.game.player.human.HumanPlayer;
import org.atorma.tictactoe.game.player.mcts.MCTSParameters;
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

    private int playerInfoIdSequence = 1;

    private List<PlayerInfo> playerInfoList = new ArrayList<>();
    private Map<String, Class<? extends Player>> playerClasses = new HashMap<>();
    private Map<String, Object> playerConfigs = new HashMap<>();

    public InMemoryPlayerRegistry() {
        addPlayerInfo("Human", PlayerInfo.Type.HUMAN, HumanPlayer.class);

        MCTSParameters naiveParams = new MCTSParameters();
        naiveParams.gamesPerRollout = parseSystemPropertyAsInt("MCTS_NAIVE_GAMES_PER_ROLLOUT", 50);
        naiveParams.simulationStrategy = MCTSParameters.SimulationStrategy.NAIVE;
        addPlayerInfo("MCTS naive heuristics", PlayerInfo.Type.AI, MCTSPlayer.class, naiveParams);

        MCTSParameters randomAdjacentParams = new MCTSParameters();
        randomAdjacentParams.simulationStrategy = MCTSParameters.SimulationStrategy.RANDOM_ADJACENT;
        randomAdjacentParams.gamesPerRollout =  naiveParams.gamesPerRollout = parseSystemPropertyAsInt("MCTS_RANDOM_ADJACENT_GAMES_PER_ROLLOUT", 200);
        addPlayerInfo("MCTS random adjacent", PlayerInfo.Type.AI, MCTSPlayer.class, randomAdjacentParams);

        MCTSParameters uniformRandomParams = new MCTSParameters();
        uniformRandomParams.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        uniformRandomParams.gamesPerRollout = parseSystemPropertyAsInt("MCTS_UNIFORM_RANDOMN_GAMES_PER_ROLLOUT", 700);
        uniformRandomParams.searchRadius = 2;
        addPlayerInfo("MCTS uniform random", PlayerInfo.Type.AI, MCTSPlayer.class, uniformRandomParams);

        addPlayerInfo("Naive heuristics", PlayerInfo.Type.AI, NaivePlayer.class);

        addPlayerInfo("Random adjacent", PlayerInfo.Type.AI, RandomAdjacentPlayer.class);

        addPlayerInfo("Uniform random", PlayerInfo.Type.AI, RandomPlayer.class);
    }

    private int parseSystemPropertyAsInt(String property, int defaultValue) {
        return Integer.parseInt(
                Optional.
                        ofNullable(System.getProperty(property))
                        .orElse(String.valueOf(defaultValue))
        );
    }

    private PlayerInfo addPlayerInfo(String name, PlayerInfo.Type type, Class<? extends Player> playerClass, Object configuration) {
        PlayerInfo playerInfo = addPlayerInfo(name, type, playerClass);
        playerConfigs.put(playerInfo.getId(), configuration);
        return playerInfo;
    }

    private PlayerInfo addPlayerInfo(String name, PlayerInfo.Type type, Class<? extends Player> playerClass) {
        PlayerInfo playerInfo = new PlayerInfo(String.valueOf(playerInfoIdSequence), name, type);
        playerInfoIdSequence++;
        playerInfoList.add(playerInfo);
        playerClasses.put(playerInfo.getId(), playerClass);
        return playerInfo;
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
            if (player instanceof Configurable) {
                Configurable configurable = (Configurable) player;
                configurable.configure(playerConfigs.get(playerInfo.getId()));
            }
            // TODO configure with user input
            return player;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Error when creating player", e);
        }
    }
}
