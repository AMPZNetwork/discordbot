package com.ampznetwork.discordbot;

import com.ampznetwork.discordbot.github.GithubApiWrapper;
import org.comroid.api.model.Authentication;

public class ManualGhApiTest {
    public static void main(String[] args) {
        var api = new GithubApiWrapper(Authentication.ofToken(args[0]));
        api.updateFileContent("burdoto", "burdoto", "test.tmp", "hello world").join();
    }
}
