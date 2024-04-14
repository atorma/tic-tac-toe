package org.atorma.tictactoe.application.jackson;

import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import org.atorma.tictactoe.application.PlayerInfo;
import org.atorma.tictactoe.application.PlayerRegistry;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;

@JsonPOJOBuilder
@Configurable(autowire = Autowire.BY_TYPE, preConstruction = true)
public class PlayerInfoBuilder {

    private PlayerInfo playerInfo;

    @JsonCreator
    public PlayerInfoBuilder(@JsonProperty("id") String id, @JacksonInject ApplicationContext appCtx) {
        playerInfo = appCtx.getBean(PlayerRegistry.class).getPlayerInfoById(id);
    }

    public PlayerInfo build() {
        return playerInfo;
    }
}
