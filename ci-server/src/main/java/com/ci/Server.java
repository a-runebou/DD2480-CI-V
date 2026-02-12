package com.ci;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ci.pipeline.CIPipeline;
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

    private final ExecutorService exec;
    private final CIPipeline pipeline;

    /**
     * Production constructor: uses real pipeline and executor.
     */
    public Server() {
        this(new CIPipeline(), Executors.newFixedThreadPool(2));
    }

    /**
     * Test constructor: allows injection of mock/fake pipeline and executor.
     * @param pipeline the CI pipeline to use for processing webhooks
     * @param exec the executor service for running pipeline tasks
     */
    public Server(CIPipeline pipeline, ExecutorService exec) {
        this.server = null;
        this.pipeline = pipeline;
        this.exec = exec;
        this.dbHandler = new DbHandler();
        this.pipeline.setDbHandler(this.dbHandler);
        dbHandler.createBuildTable();
    }

    /**
     * Constructor for testing with a specific database.
     * @param dbUrl the database URL to use
     */
    public Server(String dbUrl) {
        this(new CIPipeline(), Executors.newFixedThreadPool(2), dbUrl);
    }

    /**
     * Full test constructor: allows injection of pipeline, executor, and database.
     * @param pipeline the CI pipeline to use for processing webhooks
     * @param exec the executor service for running pipeline tasks
     * @param dbUrl the database URL to use
     */
    public Server(CIPipeline pipeline, ExecutorService exec, String dbUrl) {
        this.server = null;
        this.pipeline = pipeline;
        this.exec = exec;
        this.dbHandler = new DbHandler(dbUrl);
        this.pipeline.setDbHandler(this.dbHandler);
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
        this.server.createContext("/webhook", exchange -> handleRequest(exchange, pipeline, exec));
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
        if (this.exec != null) {
            this.exec.shutdownNow();
        }
    }

    /**
     * Handles incoming HTTP requests.
     * @param exchange the HTTP exchange containing request and response data.
     * @param pipeline the CI pipeline to run for valid webhooks.
     * @param exec the executor service for running pipeline tasks.
     * @throws IOException if an I/O error occurs.
     */
    public static void handleRequest(HttpExchange exchange, CIPipeline pipeline, ExecutorService exec) throws IOException {
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

            exec.submit(() -> {
                try {
                    pipeline.run(repoUrl, branch, sha);
                } catch (Exception e) {
                    // Just log - pipeline already handles status reporting
                    e.printStackTrace();
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
