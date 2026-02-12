package com.ci.rest;

import java.io.IOException;
import java.util.List;

import com.ci.BuildEntry;
import com.ci.DbHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * The AllBuildsHandler class is responsible for handling HTTP GET requests to the /builds endpoint.
 * It retrieves all build entries from the database and responds with a JSON array of these entries.
 * If a request method other than GET is used, it responds with a 405 Method Not Allowed status code.
 */
public class AllBuildsHandler implements HttpHandler{
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final DbHandler dbHandler;
    public AllBuildsHandler(DbHandler dbHandler) {
        this.dbHandler = dbHandler;
    }
    /**
     * Handles incoming HTTP GET requests to retrieve all build entries.
     * Responds with a JSON array of all build entries.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }
            List<BuildEntry> builds = dbHandler.selectAllBuilds();
            String response = objectMapper.writeValueAsString(builds);

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        }
    }
}