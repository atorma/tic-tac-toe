package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.FastTests;
import org.atorma.tictactoe.game.player.Player;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(FastTests.class)
public class PlayerRegistryTests extends ApplicationTests {

    @Autowired PlayerRegistry playerRegistry;
    Random random = new Random();

    @Test
    public void get_player_information() {
        List<PlayerInfo> playerInfoList = playerRegistry.getPlayerInformation();
        assertThat(playerInfoList.size(), greaterThan(0));
    }

    @Test
    public void create_player() {
        List<PlayerInfo> playerInfoList = playerRegistry.getPlayerInformation();
        PlayerInfo playerInfo = playerInfoList.get(random.nextInt(playerInfoList.size()));

        Player player = playerRegistry.createPlayer(playerInfo);
        assertThat(player, notNullValue());
    }

}
