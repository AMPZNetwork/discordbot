package com.ampznetwork.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.comroid.api.func.util.Command;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;

@SpringBootApplication
public class DiscordBot {
    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
    }

    @Bean
    public Config config() {
        return Config.builder().build();
    }

    @Bean
    public JDA jda(@Autowired Config config) {
        return JDABuilder.createLight(config.resolveToken(), Arrays.asList(GatewayIntent.values())).build();
    }

    @Bean
    public Command.Manager cmdr(@Autowired JDA jda) {
        return new Command.Manager() {{
            new Adapter$JDA(jda);
            new Adapter$StdIO();
            addChild(DiscordBot.this);
            register(BotCommands.class);
        }};
    }
}
