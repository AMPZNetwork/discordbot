package com.ampznetwork.discordbot;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.channel.concrete.NewsChannel;
import org.comroid.annotations.Default;
import org.comroid.api.config.Adapt;
import org.comroid.api.config.WriteOnly;
import org.comroid.api.config.adapter.impl.JdaTypeAdapter;
import org.comroid.api.data.seri.DataNode;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.info.Log;
import org.comroid.api.java.ResourceLoader;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

@Data
@NoArgsConstructor
public class Config implements DataNode {
    final Discord discord = new Discord();
    final GitHub  github  = new GitHub();
    long presentationChannelId = 1350171082405052524L;

    @SneakyThrows
    public @Nullable String resolveDiscordToken() {
        try {
            return DelegateStream.readAll(ResourceLoader.fromResourceString(getDiscord().token)).replaceAll("\r?\n", "").trim();
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not resolve token from resource string '" + getDiscord().token + "'", t);
            return null;
        }
    }

    @SneakyThrows
    public @Nullable String resolveGithubToken() {
        try {
            return DelegateStream.readAll(ResourceLoader.fromResourceString(getGithub().token)).replaceAll("\r?\n", "").trim();
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not resolve token from resource string '" + getGithub().token + "'", t);
            return null;
        }
    }

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Discord implements DataNode {
        @WriteOnly                                                   String      token = "@/srv/cred/discord/ampz_bot.txt";
        @Adapt(JdaTypeAdapter.class) @Default("495513877103116308L") NewsChannel announcementChannel;
    }

    @Data
    @NoArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GitHub implements DataNode {
        @WriteOnly String token = "@/srv/cred/github/comroid-commit.txt";
        String repositoryOwner          = "AMPZNetwork";
        String repositoryName           = "information";
        String announcementsFilePath    = "Website/data/homepage/announcements.json";
        String announcementsFilePattern = "{\\\"message\\\":\\\"$message\\\"}";
        String commitMessage            = "Automated announcement forwarder";
    }
}
