package com.ampznetwork.discordbot.announce;

import com.ampznetwork.discordbot.github.GithubApiWrapper;
import com.ampznetwork.discordbot.util.ApplicationContextProvider;
import net.dv8tion.jda.api.JDA;
import org.comroid.api.text.Markdown;
import org.comroid.api.text.TextDecoration;
import org.intellij.lang.annotations.Language;

public enum AnnouncementChannel {
    DiscordAnnouncementChannel {
        @Override
        public void push(String announcement) {
            ApplicationContextProvider.wrap(JDA.class)
                    .map(jda -> jda.getTextChannelById(495513877103116308L))
                    .ifPresent(gc -> gc.sendMessage(announcement).queue());
        }
    }, OnlineJsonResource {
        @Override
        public void push(String announcement) {
            ApplicationContextProvider.wrap(GithubApiWrapper.class)
                    .ifPresent(gaw -> gaw.updateFileContent("AMPZNetwork", "information", "Website/data/homepage/announcements.json", """
                            {"announcement":"%s"}""".formatted(TextDecoration.sanitize(announcement.replace("\"", "\\\""), Markdown.class))).join());
        }
    };

    public abstract void push(@Language("Markdown") String announcement);
}
