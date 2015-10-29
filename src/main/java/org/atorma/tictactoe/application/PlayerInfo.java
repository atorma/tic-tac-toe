package org.atorma.tictactoe.application;

import org.springframework.util.Assert;

public class PlayerInfo {

    private String id;
    private String name;

    public PlayerInfo(String id, String name) {
        Assert.hasText(id);
        Assert.hasText(name);
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
