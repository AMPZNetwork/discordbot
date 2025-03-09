package com.ampznetwork.discordbot;

import com.ampznetwork.discordbot.io.DiscordChannelInputStream;
import com.ampznetwork.discordbot.io.DiscordChannelOutputStream;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.comroid.api.attr.Named;
import org.comroid.api.func.util.Command;
import org.comroid.api.os.OS;
import org.comroid.api.text.StringMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class BotCommands {
    @Command(permission = "8")
    public static Object promote(@Command.Arg User user, @Command.Arg String minecraft, @Command.Arg PromotionTarget target) {
        return "wip";
    }

    @Command(permission = "8")
    public static Object announce(@Command.Arg String message) {
        return "wip";
    }

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
    public enum PromotionTarget implements Named {
        user(774034225304698940L),
        tester(1010851881813299291L),
        builder(1079483093058072627L),
        helper(523986482541756430L),
        moderator(495515597942292511L),
        developer(1312325120395837460L);

        long discordRoleId;
    }

    @Command(permission = "8")
    public static class whitelist {
        @Command(permission = "8")
        public static void add(MessageChannelUnion channel, @Command.Arg String server, @Command.Arg String username) {
            execute.minecraft(channel, server, "whitelist add " + username);
        }
    }

    @Value
    private static class Forwarder implements Runnable {
        InputStream  input;
        OutputStream output;

        @Override
        @SneakyThrows
        @SuppressWarnings("InfiniteLoopStatement")
        public void run() {
            while (true) input.transferTo(output);
        }
    }

    @Command(permission = "8")
    public static class execute {
        private static final String PROCESS_FINISHED = "Process finished with exit code ";

        @Command(permission = "8")
        public static String shell(MessageChannelUnion channel, @Command.Arg(stringMode = StringMode.GREEDY) String script) {
            try {
                File           tmp = null;
                ProcessBuilder proc;
                if (!OS.isWindows) {
                    tmp = File.createTempFile("script", ".sh");
                    try (var fis = new FileOutputStream(tmp)) {
                        fis.write(script.getBytes(StandardCharsets.US_ASCII));
                    }
                    proc = new ProcessBuilder("bash", tmp.getAbsolutePath());
                } else proc = new ProcessBuilder("cmd.exe", "/c", script);

                try (var pool = Executors.newCachedThreadPool()) {
                    try (
                            var dcIn = new DiscordChannelInputStream(channel); var dcOut = new DiscordChannelOutputStream(channel, "ℹ️ - ");
                            var dcErr = new DiscordChannelOutputStream(channel, "⚠️ - ");
                    ) {
                        var exec = proc.start();
                        pool.execute(new Forwarder(dcIn, exec.getOutputStream()));
                        pool.execute(new Forwarder(exec.getInputStream(), dcOut));
                        pool.execute(new Forwarder(exec.getErrorStream(), dcErr));

                        return PROCESS_FINISHED + exec.waitFor();
                    }
                } finally {
                    if (tmp != null)//noinspection ResultOfMethodCallIgnored
                        tmp.delete();
                }
            } catch (InterruptedException | IOException e) {
                throw new RuntimeException("A fatal unexpected error has occurred", e);
            }
        }

        @Command(permission = "8")
        public static void minecraft(MessageChannelUnion channel, @Command.Arg String server, @Command.Arg(stringMode = StringMode.GREEDY) String command) {
            shell(channel, "echo \"%s\">>/opt/mc/%s/stdin".formatted(command, server));
        }
    }
}
