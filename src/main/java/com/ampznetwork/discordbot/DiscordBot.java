package com.ampznetwork.discordbot;

import com.ampznetwork.discordbot.github.GithubApiWrapper;
import com.ampznetwork.discordbot.util.ApplicationContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.comroid.api.func.util.Command;
import org.comroid.api.model.Authentication;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Arrays;

@Component
@SpringBootApplication
@ComponentScan(basePackageClasses = ApplicationContextProvider.class)
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
    public @Nullable Authentication discordAuthentication(@Autowired Config config) {
        var token = config.resolveDiscordToken();
        return token == null ? null : Authentication.ofToken(token);
    }

    @Bean
    @ConditionalOnExpression("discordAuthentication != null")
    public JDA jda(@Autowired Authentication discordAuthentication) {
        return JDABuilder.createLight(discordAuthentication.getPasskey(), Arrays.asList(GatewayIntent.values())).build();
    }

    @Bean
    @ConditionalOnBean(JDA.class)
    public Command.Manager.Adapter$JDA cmdrDiscord(@Autowired Command.Manager cmdr, @Autowired JDA jda) {
        var adapter$JDA = cmdr.new Adapter$JDA(jda);
        cmdr.register(BotCommands.class);
        cmdr.initialize();
        return adapter$JDA;
    }

    @Bean
    public @Nullable Authentication githubAuthentication(@Autowired Config config) {
        var token = config.resolveGithubToken();
        if (token == null) return null;
        var fName = new File(config().getGithubToken()).getName();
        return Authentication.ofToken(fName.substring(0, fName.indexOf('.')), token);
    }

    @Bean
    @ConditionalOnExpression("githubAuthentication != null")
    public GithubApiWrapper githubApi(@Autowired Authentication githubAuthentication) {
        return new GithubApiWrapper(githubAuthentication);
    }
}
