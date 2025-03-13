package com.ampznetwork.discordbot.github;

import com.ampznetwork.discordbot.github.model.GitComitter;
import com.ampznetwork.discordbot.github.model.GitFileInfo;
import com.ampznetwork.discordbot.github.model.GitFileUpdateRequest;
import lombok.Value;
import org.comroid.api.comp.Base64;
import org.comroid.api.model.Authentication;
import org.comroid.api.net.REST;

import java.util.concurrent.CompletableFuture;

@Value
public class GithubApiWrapper {
    Authentication authentication;

    private REST.Request request(REST.Method method, String url) {
        return REST.request(method, url)
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", "Bearer " + authentication.getPasskey())
                .addHeader("X-GitHub-Api-Version", "2022-11-28");
    }

    public CompletableFuture<GitFileInfo> retrieveFileInfo(String repoOwner, String repoName, String filePath) {
        return request(REST.Method.GET, "https://api.github.com/repos/%s/%s/contents/%s".formatted(repoOwner, repoName, filePath)).execute()
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().as(GitFileInfo.class).assertion());
    }

    public CompletableFuture<String> retrieveFileContent(String repoOwner, String repoName, String filePath) {
        return retrieveFileInfo(repoOwner, repoName, filePath).thenApply(gfi -> gfi.getContent().getDownload_url())
                .thenCompose(REST::get)
                .thenApply(REST.Response::validate2xxOK)
                .thenApply(rsp -> rsp.getBody().toSerializedString());
    }

    public CompletableFuture<REST.Response> updateFileContent(String repoOwner, String repoName, String filePath, String content) {
        return retrieveFileInfo(repoOwner, repoName, filePath)
                .thenApply(gfi -> gfi.getContent().getSha())
                .exceptionally(t -> null /* assume file does not exist; set to null to create new file */)
                .thenCompose(sha -> request(REST.Method.PUT, "https://api.github.com/repos/%s/%s/contents/%s".formatted(repoOwner, repoName, filePath))
                        .setBody(new GitFileUpdateRequest("Automated content update", new GitComitter("Kaleidox", "robot@kaleidox.de"),
                                Base64.encode(content), sha))
                        .execute());
    }
}
