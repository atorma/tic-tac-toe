package org.atorma.tictactoe.game.player.mcts;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MCTSTreeNodeDTO {
    public final String id;
    public final Set<MCTSTreeNodeDTO> children = new HashSet<>();


    public MCTSTreeNodeDTO(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MCTSTreeNodeDTO that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
