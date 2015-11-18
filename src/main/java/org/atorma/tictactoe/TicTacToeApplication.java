package org.atorma.tictactoe;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@SpringBootApplication
public class TicTacToeApplication {

    @Autowired private ApplicationContext applicationContext;

    @Bean
    public WebSecurityConfigurerAdapter webSecurityConfigurerAdapter() {
        return new MyWebSecurityConfigurerAdapter();
    }

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


    public static class MyWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .authorizeRequests()
                    .antMatchers("/").permitAll()
                    .antMatchers("/players/**").permitAll()
                    .antMatchers("/games/**").permitAll()
                    .antMatchers("/*.js").permitAll()
                    .antMatchers("/resources/**").permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .httpBasic();
        }
    }
}
