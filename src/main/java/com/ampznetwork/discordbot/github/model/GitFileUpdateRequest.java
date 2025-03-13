package com.ampznetwork.discordbot.github.model;

import lombok.Value;
import org.comroid.api.data.seri.DataNode;
import org.jetbrains.annotations.Nullable;

@Value
public class GitFileUpdateRequest implements DataNode {
    String      message;
    GitComitter comitter;
    String      content;
    @Nullable String sha;
}
