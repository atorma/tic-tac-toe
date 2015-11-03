package org.atorma.tictactoe.controller;

import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerRegistryController {

    private PlayerRegistry playerRegistry;


    @RequestMapping(method = RequestMethod.GET)
    public List<PlayerInfo> getPlayerList() {
        return playerRegistry.getPlayerInformation();
    }


    @Autowired
    public void setPlayerRegistry(PlayerRegistry playerRegistry) {
        this.playerRegistry = playerRegistry;
    }
}
