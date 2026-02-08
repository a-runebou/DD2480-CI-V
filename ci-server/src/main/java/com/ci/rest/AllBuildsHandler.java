package com.ci.rest;

import java.io.IOException;
import java.util.List;

import com.ci.BuildEntry;
import com.ci.DbHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class AllBuildsHandler implements HttpHandler{
    private static ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            return;
        }

        DbHandler handler = new DbHandler();
        List<BuildEntry> builds = handler.selectAllBuilds();
        String response = objectMapper.writeValueAsString(builds);

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}