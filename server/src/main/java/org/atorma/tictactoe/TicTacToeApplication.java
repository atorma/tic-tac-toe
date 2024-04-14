package org.atorma.tictactoe;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@SpringBootApplication
public class TicTacToeApplication {

    @Autowired private ApplicationContext applicationContext;

    @Bean @Primary
    public ObjectMapper defaultObjectMapper() {
        InjectableValues inject = new InjectableValues.Std()
                .addValue(ApplicationContext.class, applicationContext);

        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        ObjectMapper objectMapper = builder.build();
        objectMapper.setInjectableValues(inject);
        return objectMapper;
    }

    public static void main(String[] args) {
        SpringApplication.run(TicTacToeApplication.class, args);
    }

}
