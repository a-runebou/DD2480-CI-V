package com.ci.statuses;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The StatusPoster class is responsible for posting status updates to GitHub for a specific commit.
 * It reads the necessary configuration from a properties file and uses the GitHub API to create commit statuses.
 */
public class StatusPoster {
    private String repo;
    private String owner;
    private String token;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();
    
    /**
     * Constructor for the StatusPoster.
     * 
     * @throws IllegalStateException - if there arises a problem with parameter loading
     */
    public StatusPoster() {
        Properties prop = new Properties();
        try (InputStream input =
                 getClass().getClassLoader().getResourceAsStream("token.config")) {
            if (input == null) {
                throw new IllegalStateException("token.config not found on classpath.");
            }
            prop.load(input);
            this.token = prop.getProperty("token").trim();
            this.owner = prop.getProperty("owner").trim();
            this.repo = prop.getProperty("repo").trim();
            if (repo == null || owner == null || token == null) {
                throw new IllegalStateException("token.config must define token, owner and repo.");
        }
        } catch (IOException ex) {
            throw new IllegalStateException("Error reading token.config file: " + ex.getMessage());
        }
    }

    /**
     * Posts a status to a commit identified by its sha.
     * 
     * @param sha the SHA of the commit
     * @param state state of the status - {error, failure, pending, success}
     * @param targetUrl target URL to associate with this status
     * @param description description for the commit status
     * @throws IOException if an I/O error occurs when sending or receiving, or the client has shut down
     * @throws InterruptedException if the operation of the client is interrupted
     * @throws IllegalArgumentException if the state contains invalid value
     * @throws RuntimeException if the GitHub API responds with an error
     */
    public void postStatus(String sha, String state, String targetUrl, String description) 
        throws IOException, InterruptedException {
        if (!Set.of("error", "failure", "pending", "success").contains(state)) {
            throw new IllegalArgumentException("Invalid state: " + state);
        }
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("state", state);
        root.put("target_url", targetUrl);
        root.put("description", description);
        root.put("context", "continuous-integration");
        // create the body
        String body = mapper.writeValueAsString(root);
        // create the github api url
        String gitUrl = "https://api.github.com/repos/"+this.owner+"/"+this.repo+"/statuses/"+sha;
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .uri(URI.create(gitUrl))
                .header("Accept", "application/vnd.github+json") // recommended value by GitHub API
                .header("Authorization", "Bearer "+this.token)
                .header("X-GitHub-Api-Version","2022-11-28")
                .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode()<200 || response.statusCode() >= 300) { // 200 - OK, 201 - status created
            throw new RuntimeException("Failed to create commit status, code "+response.statusCode() + ": "+response.body());
        } 
    }
}
