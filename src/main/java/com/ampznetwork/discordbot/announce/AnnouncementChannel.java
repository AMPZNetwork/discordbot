package com.ampznetwork.discordbot.announce;

import com.ampznetwork.discordbot.github.GithubApiWrapper;
import com.ampznetwork.discordbot.util.ApplicationContextProvider;
import net.dv8tion.jda.api.JDA;
import org.comroid.api.text.Markdown;
import org.comroid.api.text.TextDecoration;
import org.intellij.lang.annotations.Language;

import java.util.concurrent.CompletableFuture;

public enum AnnouncementChannel {
    DiscordAnnouncementChannel {
        @Override
        public CompletableFuture<?> push(String announcement) {
            return ApplicationContextProvider.wrap(JDA.class)
                    .map(jda -> jda.getTextChannelById(495513877103116308L))
                    .map(gc -> gc.sendMessage(announcement).submit())
                    .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException("Discord Bot not initialized")));
        }
    }, OnlineJsonResource {
        @Override
        public CompletableFuture<?> push(String announcement) {
            return ApplicationContextProvider.wrap(GithubApiWrapper.class)
                    .map(gaw -> gaw.updateFileContent("AMPZNetwork", "information", "Website/data/homepage/announcements.json", """
                            {"announcement":"%s"}""".formatted(TextDecoration.sanitize(announcement.replace("\"", "\\\""), Markdown.class))))
                    .orElseGet(() -> CompletableFuture.failedFuture(new RuntimeException("GitHub API not initialized")));
        }
    };

    public abstract CompletableFuture<?> push(@Language("Markdown") String announcement);
}
