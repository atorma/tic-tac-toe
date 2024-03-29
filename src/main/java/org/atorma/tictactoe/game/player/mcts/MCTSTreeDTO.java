package org.atorma.tictactoe.game.player.mcts;

import java.util.Map;

public record MCTSTreeDTO(String rootNodeId, Map<String, MoveNodeDTO> nodes) { }
