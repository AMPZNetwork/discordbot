package com.ampznetwork.discordbot.io;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.comroid.api.func.util.Debug;
import org.comroid.api.func.util.Ratelimit;
import org.comroid.api.text.Markdown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public class DiscordChannelOutputStream extends OutputStream {
    MessageChannelUnion channel;
    @Nullable Message replyTo;
    @Nullable String  prefix;
    Queue<@NotNull Byte>                        data        = new LinkedList<>();
    AtomicReference<CompletableFuture<Message>> MESSAGE_REF = new AtomicReference<>(CompletableFuture.completedFuture(null));

    public DiscordChannelOutputStream(MessageChannelUnion channel) {
        this(channel, null, null);
    }

    public DiscordChannelOutputStream(MessageChannelUnion channel, @Nullable String prefix) {
        this(channel, null, prefix);
    }

    public DiscordChannelOutputStream(MessageChannelUnion channel, @Nullable Message replyTo) {
        this(channel, replyTo, null);
    }

    public DiscordChannelOutputStream(MessageChannelUnion channel, @Nullable Message replyTo, @Nullable String prefix) {
        this.channel = channel;
        this.replyTo = replyTo;
        this.prefix  = prefix;
    }

    @Override
    public void write(int b) {
        synchronized (data) {
            if (((char) b) == '\n') flush();
            else data.add((byte) b);
        }
    }

    @Override
    public void flush() {
        byte[] data;
        synchronized (this.data) {
            Byte[] buf;
            buf = this.data.toArray(Byte[]::new);
            this.data.clear();

            data = new byte[buf.length];
            for (var i = 0; i < buf.length; i++)
                data[i] = buf[i];
        }

        var text = new String(data);
        Ratelimit.run(text, Duration.ofSeconds(1), MESSAGE_REF, (existing, lines) -> {
            var output = String.join("\n", lines);
            lines.clear();

            output = Markdown.CodeBlock.apply(output);
            if (replyTo == null) return channel.sendMessage(output).submit();
            else return replyTo.reply(output).submit();
        }).exceptionally(Debug.exceptionLogger("Could not send text to Discord"));
    }
}
