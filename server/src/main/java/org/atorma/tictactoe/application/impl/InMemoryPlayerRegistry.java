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
import org.atorma.tictactoe.game.player.random.RandomNearbyPlayer;
import org.atorma.tictactoe.game.player.random.RandomPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Service
public class InMemoryPlayerRegistry implements PlayerRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryPlayerRegistry.class);

    private int playerInfoIdSequence = 1;

    private final List<PlayerInfo> playerInfoList = new ArrayList<>();
    private final Map<String, Class<? extends Player>> playerClasses = new HashMap<>();
    private final Map<String, Object> playerConfigs = new HashMap<>();

    public InMemoryPlayerRegistry() {
        addPlayerInfo("Human", PlayerInfo.Type.HUMAN, HumanPlayer.class);

        MCTSParameters naiveParams = new MCTSParameters();
        naiveParams.gamesPerRollout = parseEnvVarAsInt("MCTS_NAIVE_GAMES_PER_ROLLOUT", 10);
        naiveParams.pruneDescendantLevelsGreaterThan = parseEnvVarAsInt("MCTS_MAX_DESCENDANT_LEVEL", Integer.MAX_VALUE);
        naiveParams.simulationStrategy = MCTSParameters.SimulationStrategy.NAIVE;

        addPlayerInfo("MCTS naive heuristics", PlayerInfo.Type.AI, MCTSPlayer.class, naiveParams);

        MCTSParameters randomNearbyParams = new MCTSParameters();
        randomNearbyParams.simulationStrategy = MCTSParameters.SimulationStrategy.RANDOM_NEARBY;
        randomNearbyParams.gamesPerRollout = parseEnvVarAsInt("MCTS_RANDOM_NEARBY_GAMES_PER_ROLLOUT", 25);
        randomNearbyParams.searchRadius = 2;
        randomNearbyParams.pruneDescendantLevelsGreaterThan = parseEnvVarAsInt("MCTS_MAX_DESCENDANT_LEVEL", Integer.MAX_VALUE);
        addPlayerInfo("MCTS random nearby", PlayerInfo.Type.AI, MCTSPlayer.class, randomNearbyParams);

        MCTSParameters uniformRandomParams = new MCTSParameters();
        uniformRandomParams.simulationStrategy = MCTSParameters.SimulationStrategy.UNIFORM_RANDOM;
        uniformRandomParams.gamesPerRollout = parseEnvVarAsInt("MCTS_UNIFORM_RANDOM_GAMES_PER_ROLLOUT", 700);
        uniformRandomParams.searchRadius = 2;
        uniformRandomParams.pruneDescendantLevelsGreaterThan = parseEnvVarAsInt("MCTS_MAX_DESCENDANT_LEVEL", Integer.MAX_VALUE);
        addPlayerInfo("MCTS uniform random", PlayerInfo.Type.AI, MCTSPlayer.class, uniformRandomParams);

        addPlayerInfo("Naive heuristics", PlayerInfo.Type.AI, NaivePlayer.class);

        addPlayerInfo("Random adjacent", PlayerInfo.Type.AI, RandomNearbyPlayer.class);

        addPlayerInfo("Uniform random", PlayerInfo.Type.AI, RandomPlayer.class);
    }

    private int parseEnvVarAsInt(String property, int defaultValue) {
        return Integer.parseInt(
                Optional.
                        ofNullable(System.getenv(property))
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
        var playerInfo = playerInfoList.stream()
                .filter(x -> x.getId().equals(id))
                .findFirst();
        if (playerInfo.isPresent()) {
            return playerInfo.get();
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
            Player player = playerClass.getDeclaredConstructor().newInstance();
            if (player instanceof Configurable configurable) {
                configurable.configure(playerConfigs.get(playerInfo.getId()));
            }
            // TODO configure with user input
            return player;
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error when creating player", e);
        }
    }
}
