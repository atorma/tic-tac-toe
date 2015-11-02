package org.atorma.tictactoe.application;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.util.Assert;

public class PlayerInfo {

    private String id;
    private String name;

    @JsonCreator
    public PlayerInfo(@JsonProperty("id") String id, @JsonProperty("name") String name) {
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
