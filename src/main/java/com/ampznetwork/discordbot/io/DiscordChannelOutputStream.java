package com.ampznetwork.discordbot.io;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.NonFinal;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.comroid.api.text.Markdown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;

@Value
@NonFinal
@EqualsAndHashCode(callSuper = true)
public class DiscordChannelOutputStream extends OutputStream {
    MessageChannelUnion channel;
    @Nullable Message replyTo;
    @Nullable String  prefix;
    Queue<@NotNull Byte> data = new LinkedList<>();

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
            System.arraycopy(buf, 0, data, 0, buf.length);
        }

        var text = new String(data);
        text = Markdown.CodeBlock.apply(text);
        if (replyTo == null) channel.sendMessage(text).queue();
        else replyTo.reply(text).queue();
    }
}
