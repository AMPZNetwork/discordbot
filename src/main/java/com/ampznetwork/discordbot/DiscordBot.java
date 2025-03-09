package com.ampznetwork.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.comroid.api.func.util.Command;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@SpringBootApplication
public class DiscordBot {
    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
    }

    private @Autowired Command.Manager cmdr;

    @Bean
    public Config config() {
        return Config.builder().build();
    }

    @Bean
    public Command.Manager cmdr() {
        return new Command.Manager() {{
            new Adapter$StdIO();
        }};
    }

    @Bean
    public @Nullable String discordToken(@Autowired Config config) {
        return config.resolveToken();
    }

    @Bean
    @ConditionalOnExpression("discordToken != null")
    public JDA jda(@Autowired String discordToken) {
        return JDABuilder.createLight(discordToken, Arrays.asList(GatewayIntent.values())).build();
    }

    @Bean
    @ConditionalOnBean(JDA.class)
    public Command.Manager.Adapter$JDA cmdrDiscord(@Autowired Command.Manager cmdr, @Autowired JDA jda) {
        var adapter$JDA = cmdr.new Adapter$JDA(jda);
        cmdr.register(BotCommands.class);
        cmdr.initialize();
        return adapter$JDA;
    }
}
