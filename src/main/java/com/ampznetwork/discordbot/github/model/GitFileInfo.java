package com.ampznetwork.discordbot.github.model;

import lombok.Value;

@Value
public class GitFileInfo {
    Descriptor content;

    @Value
    public static class Descriptor {
        String name;
        String path;
        String sha;
        long   size;
        String url;
        String html_url;
        String git_url;
        String download_url;
    }
}
