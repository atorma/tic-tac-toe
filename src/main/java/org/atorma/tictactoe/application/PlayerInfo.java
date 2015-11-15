package org.atorma.tictactoe.application;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.atorma.tictactoe.application.jackson.PlayerInfoBuilder;
import org.springframework.util.Assert;

@JsonDeserialize(builder = PlayerInfoBuilder.class)
public class PlayerInfo {

    public enum Type {
        AI, HUMAN
    }

    private final String id;
    private final String name;
    private final Type type;

    public PlayerInfo(String id, String name, Type type) {
        Assert.hasText(id);
        Assert.hasText(name);
        Assert.notNull(type);
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PlayerInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
