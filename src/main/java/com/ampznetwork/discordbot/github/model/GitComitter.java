package com.ampznetwork.discordbot.github.model;

import lombok.Value;
import org.comroid.api.data.seri.DataNode;

@Value
public class GitComitter implements DataNode {
    String name;
    String email;
}
