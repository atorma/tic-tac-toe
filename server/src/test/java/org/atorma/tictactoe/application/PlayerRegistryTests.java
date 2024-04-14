package org.atorma.tictactoe.application;

import org.atorma.tictactoe.ApplicationTests;
import org.atorma.tictactoe.game.player.Player;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Tag("FastTests")
public class PlayerRegistryTests extends ApplicationTests {

    @Autowired PlayerRegistry playerRegistry;
    Random random = new Random();

    @Test
    public void get_player_information_list() {
        List<PlayerInfo> playerInfoList = playerRegistry.getPlayerInfoList();
        assertThat(playerInfoList.size(), greaterThan(0));
    }

    @Test
    public void get_player_information_by_id() {
        List<PlayerInfo> playerInfoList = playerRegistry.getPlayerInfoList();
        PlayerInfo playerInfo = playerInfoList.get(0);
        assertThat(playerInfo, equalTo(playerRegistry.getPlayerInfoById(playerInfo.getId())));
    }

    @Test
    public void create_player() {
        List<PlayerInfo> playerInfoList = playerRegistry.getPlayerInfoList();
        PlayerInfo playerInfo = playerInfoList.get(random.nextInt(playerInfoList.size()));

        Player player = playerRegistry.createPlayer(playerInfo);
        assertThat(player, notNullValue());
    }

}
