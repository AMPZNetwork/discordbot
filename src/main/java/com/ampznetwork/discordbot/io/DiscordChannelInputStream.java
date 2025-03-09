package com.ampznetwork.discordbot.io;

import lombok.EqualsAndHashCode;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public class DiscordChannelInputStream extends InputStream implements EventListener {
    MessageChannelUnion channel;
    @Nullable Message repliesOn;
    Queue<@NotNull String> lines = new LinkedBlockingQueue<>();
    @NonFinal @Nullable String buffer = null;
    @NonFinal           int    index  = -1;

    public DiscordChannelInputStream(MessageChannelUnion channel) {
        this(channel, null);
    }

    public DiscordChannelInputStream(MessageChannelUnion channel, @Nullable Message repliesOn) {
        this.channel   = channel;
        this.repliesOn = repliesOn;

        channel.getJDA().addEventListener(this);
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (!(event instanceof MessageReceivedEvent mre)) return;
        if (repliesOn != null && Optional.ofNullable(mre.getMessage().getReferencedMessage()).filter(msg -> msg.equals(repliesOn)).isEmpty()) return;
        var text = mre.getMessage().getContentRaw();
        push(text);
    }

    @Override
    @SneakyThrows
    public int read() {
        if (buffer == null || index >= buffer.length()) synchronized (lines) {
            while (lines.isEmpty()) lines.wait();
            buffer = lines.poll();
            index  = 0;
        }
        return buffer.charAt(index++);
    }

    @Override
    public void close() throws IOException {
        channel.getJDA().removeEventListener(this);
        super.close();
    }

    private void push(String line) {
        synchronized (lines) {
            lines.add(line);
            lines.notify();
        }
    }
}
