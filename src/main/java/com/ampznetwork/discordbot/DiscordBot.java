package com.ampznetwork.discordbot;

import com.ampznetwork.discordbot.github.GithubApiWrapper;
import com.ampznetwork.discordbot.util.ApplicationContextProvider;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.comroid.api.config.ConfigurationManager;
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
import java.util.Optional;

@Component
@SpringBootApplication
@ComponentScan(basePackageClasses = ApplicationContextProvider.class)
public class DiscordBot {
    public static void main(String[] args) {
        SpringApplication.run(DiscordBot.class, args);
    }

    private @Autowired Command.Manager cmdr;

    @Bean
    public ConfigurationManager<Config> configManager(@Autowired ApplicationContextProvider context) {
        return new ConfigurationManager<>(context, Config.class, "/srv/discordbot/config.json");
    }

    @Bean
    public Config config(@Autowired ConfigurationManager<Config> configManager) {
        return configManager.initialize();
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
    public @Nullable ConfigurationManager<?>.Presentation$JDA configJdaPresentation(@Autowired ConfigurationManager<Config> configManager, @Autowired JDA jda) {
        var config = configManager.getConfig();
        return Optional.of(config.getPresentationChannelId())
                .filter(x -> x != 0)
                .map(jda::getTextChannelById)
                .or(() -> Optional.ofNullable(jda.getGuildById(495506209881849856L)).map(ampz -> {
                    var channel = ampz.createTextChannel("cobalton-config")
                            .addMemberPermissionOverride(776570372178837517L, 10256, 0)
                            .addPermissionOverride(ampz.getPublicRole(), 0, 3072)
                            .submit()
                            .join();
                    config.setPresentationChannelId(channel.getIdLong());
                    configManager.save();
                    return channel;
                }))
                .map(channel -> configManager.new Presentation$JDA(channel))
                .orElse(null);
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
        var fName = new File(config.getDiscord().getToken()).getName();
        return Authentication.ofToken(fName.substring(0, fName.indexOf('.')), token);
    }

    @Bean
    @ConditionalOnExpression("githubAuthentication != null")
    public GithubApiWrapper githubApi(@Autowired Authentication githubAuthentication) {
        return new GithubApiWrapper(githubAuthentication);
    }
}
