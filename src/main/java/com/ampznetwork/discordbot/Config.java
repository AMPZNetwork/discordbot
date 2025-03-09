package com.ampznetwork.discordbot;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.info.Log;
import org.comroid.api.java.ResourceLoader;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

@Value
@Builder
public class Config {
    @Default String token = "@/srv/cred/discord/ampz_bot.txt";

    @SneakyThrows
    public @Nullable String resolveToken() {
        try {
            return DelegateStream.readAll(ResourceLoader.fromResourceString(getToken())).replaceAll("\r?\n","").trim();
        } catch (Throwable t) {
            Log.at(Level.WARNING, "Could not resolve token from resource string '" + getToken() + "'", t);
            return null;
        }
    }
}
