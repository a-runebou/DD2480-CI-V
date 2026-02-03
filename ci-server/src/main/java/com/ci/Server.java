package com.ci;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class Server {
    private HttpServer server;
    private int port;
    private boolean DEBUG = true;

    public Server() {
        this.server = null;
        this.port = 1111; // Default port
    }

    /** 
     * Starts the server.
     * @throws IOException if the server fails to start.
     */
    public void start() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(this.port), 0);
        this.server.createContext("/webhook", Server::handleRequest);
        this.server.setExecutor(null);
        this.server.start();

        // Debug information
        if (DEBUG) {
            System.out.println("Server started on port " + this.port);
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
        System.out.println("Handling request...");
        
        // Only accept POST requests
        if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
            // Process the webhook payload
            System.out.println("Received POST request");

            System.out.println("Request Headers: " + exchange.getRequestHeaders().toString());
            System.out.println("Request Body: " + new String(exchange.getRequestBody().readAllBytes()));

            // Send a 200 OK response
            String response = "Webhook received";
            exchange.sendResponseHeaders(200, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } else {
            // Send a 405 Method Not Allowed response
            String response = "Method Not Allowed";
            exchange.sendResponseHeaders(405, response.length());
        }
        exchange.getResponseBody().close();
    }
}