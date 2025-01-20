package com.ampznetwork.discordbot;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.SneakyThrows;
import lombok.Value;
import org.comroid.api.func.util.DelegateStream;
import org.comroid.api.java.ResourceLoader;

@Value
@Builder
public class Config {
    @Default String token = "@/srv/cred/discord/ampz_bot.txt";

    @SneakyThrows
    public String resolveToken() {
        return DelegateStream.readAll(ResourceLoader.fromResourceString(getToken()));
    }
}
