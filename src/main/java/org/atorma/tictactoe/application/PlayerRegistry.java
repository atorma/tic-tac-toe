package org.atorma.tictactoe.application;

import org.atorma.tictactoe.game.player.Player;

import java.util.List;

public interface PlayerRegistry {

    List<PlayerInfo> getPlayerInformation();

    Player createPlayer(PlayerInfo playerInfo);

}
