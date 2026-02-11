package com.ci;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ci.checkout.GitCheckoutService;
import com.ci.statuses.StatusPoster;
import com.ci.rest.AllBuildsHandler;
import com.ci.rest.BuildByShaHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * The Server class is responsible for handling incoming HTTP requests, particularly GitHub webhook events.
 * It processes the webhook payload, checks out the relevant code from the repository, compiles it, and posts status updates to GitHub.
 * It also provides endpoints to retrieve build information from the database.
 */
public class Server {
    private HttpServer server;
    private final DbHandler dbHandler;
    private static boolean DEBUG = true;

    private static final ExecutorService EXEC = Executors.newFixedThreadPool(2);

    /**
     * Constructor for the Server class.
     * Initializes the database handler and creates the builds table if it does not exist.
     */
    public Server() {
        this.server = null;
        this.dbHandler = new DbHandler(); // Optionally specify a different database
        dbHandler.createBuildTable();
    }

    /**
     * Constructor for the Server class that allows specifying a database URL.
     * @param dbUrl
     */
    public Server(String dbUrl) { // for testing with a specific database
        this.server = null;
        this.dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
    }

    /**
     * Returns the port number the server is running on.
     * @return the port number.
     */
    public int getPort() {
        return this.server.getAddress().getPort();
    }

    /** 
     * Starts the server.
     * @param port the port number to listen on.
     * @throws IOException if the server fails to start.
     */
    public void start(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/webhook", Server::handleRequest);
        this.server.createContext("/builds", new AllBuildsHandler(this.dbHandler));
        this.server.createContext("/builds/", new BuildByShaHandler(this.dbHandler));
        this.server.setExecutor(null);
        this.server.start();

        // Debug information
        if (DEBUG) {
            System.out.println("Server started on port " + this.getPort());
        }
    }

    /**
     * Stops the server.
     */
    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }

    /**
     * Handles incoming HTTP requests.
     * @param exchange the HTTP exchange containing request and response data.
     * @throws IOException if an I/O error occurs.
     */
    public static void handleRequest(HttpExchange exchange) throws IOException {
        if (DEBUG) {
            System.out.println("Handling request...");
        }
        
        // Only accept POST requests
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // Process the webhook payload
            if (DEBUG) {
                System.out.println("Received POST request");
            }

            // Read the request body
            String body = new String(exchange.getRequestBody().readAllBytes());

            if (body.isBlank()) {
                if (DEBUG) {
                    System.out.println("Empty request body received");
                }
                String response = "Empty request body";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
                return;
            }

            if (DEBUG) {
                System.out.println("Request Body: " + body);
            }
            
            // Parse JSON payload
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode json;
            try {
                json = objectMapper.readTree(body);
            } catch (IOException e) {
                if (DEBUG) {
                    System.out.println("Failed to parse JSON payload");
                }
                String response = "Invalid JSON payload";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
                return;
            }

            // Validate JSON structure
            if (!json.has("ref") || !json.has("after")) {
                String response = "Missing required fields";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
                return;
            }

            // Get branch reference and SHA
            String ref = json.get("ref").asText();
            String sha = json.get("after").asText();
            String branch = ref.replace("refs/heads/", "");

            if (DEBUG) {
                System.out.println("Branch: " + branch);
                System.out.println("Commit SHA: " + sha);
            }

            // Get repo clone URL from payload
            JsonNode repoNode = json.get("repository");
            if (repoNode == null || !repoNode.has("clone_url")) {
                String response = "Missing repository.clone_url";
                byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(400, responseBytes.length);
                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
                return;
            }
            String repoUrl = repoNode.get("clone_url").asText();

            EXEC.submit(() -> {
                try {
                    GitCheckoutService checkout = new GitCheckoutService();
                    var dir = checkout.checkout(repoUrl, branch, sha);
                    System.out.println("[CI] Checked out into: " + dir);
                    
                    // Run mvnw compile inside the checked out repo
                    CompileRunner.Result result = CompileRunner.runMvnwCompile(dir.toFile());

                    System.out.println("[CI] mvnw compile exitCode: " + result.exitCode);

                    System.out.println("----- OUTPUT MESSAGE -----");
                    System.out.print(result.outputMessage);

                    System.out.println("----- ERRORS -----");
                    System.out.print(result.errorMessage);

                    if (result.exitCode == 0) { 
                        System.out.println("[CI] Compile succeeded"); 
                    } else {
                        StatusPoster status = new StatusPoster();                
                        status.postStatus(sha, "failure", "", "CI: Compile failed");
                        System.out.println("[CI] compile failed");
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        new StatusPoster().postStatus(sha, "error", "", "CI: server error");
                    } catch (Exception ignored) {}
                }
            });

            // Send a 200 OK response
            String response = "Webhook parsed successfully";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } else {
            // Send a 405 Method Not Allowed response
            String response = "Method Not Allowed";
            byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(405, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        }
        exchange.getResponseBody().close();
    }
}