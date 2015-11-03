package org.atorma.tictactoe.controller;

import org.atorma.tictactoe.ApplicationMvcTests;
import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PlayerRegistryRestApplicationTests extends ApplicationMvcTests {

    @Autowired PlayerRegistry playerRegistry;

    @Test
    public void get_player_list() throws Exception {
        List<PlayerInfo> playerList = playerRegistry.getPlayerInformation();

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(hasSize(playerList.size())))
                .andExpect(jsonPath("$[0].id").value(playerList.get(0).getId()))
                .andExpect(jsonPath("$[0].name").value(playerList.get(0).getName()))
        ;
    }
}
