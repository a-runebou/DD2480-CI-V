package com.ci.rest;

import java.io.IOException;

import com.ci.BuildEntry;
import com.ci.DbHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class BuildByShaHandler implements HttpHandler {
    private static ObjectMapper objectMapper = new ObjectMapper();
    private DbHandler dbHandler;
    public BuildByShaHandler(DbHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Handles incoming HTTP GET requests to retrieve build information by SHA.
     * Expects the request path to be in the format /builds/{sha}, where {sha} is the commit SHA.
     * Responds with a JSON object containing the build information for the specified SHA.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String[] segments = path.split("/");
        if (segments.length != 3) {
            exchange.sendResponseHeaders(400, -1); // Bad Request
            return;
        }
        String sha = segments[2];
        BuildEntry build = dbHandler.selectBySha(sha);
        if (build == null) {
            exchange.sendResponseHeaders(404, -1); // Not Found
            return;
        }
        String response = objectMapper.writeValueAsString(build);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
