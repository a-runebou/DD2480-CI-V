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
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            List<BuildEntry> builds = dbHandler.selectAllBuilds();

            StringBuilder html = new StringBuilder();

            html.append("<!DOCTYPE html>");
            html.append("<html>");
            html.append("<head>");
            html.append("<title>Builds</title>");
            html.append("</head>");
            html.append("<body>");

            html.append("<h1>Builds</h1>");
            html.append("<ul>");

            for (BuildEntry build : builds) {
                String id = build.id + ""; 
                String sha = build.sha;
                String branch = build.branch;
                String result = build.buildResult;
                String description = build.buildDescription;
                String date = build.buildDate;

                html.append("<li>")
                    .append("ID: ")
                    .append(id)
                    .append("</li>");

                html.append("<ul>");

                
                html.append("<li>")
                    .append("<a href=\"builds/")
                    .append(sha)
                    .append("\">")
                    .append(sha)
                    .append("</a>")
                    .append("</li>");

                html.append("<li>")
                    .append("Branch: ")
                    .append(branch)
                    .append("</li>");

                html.append("<li>")
                    .append("Result: ")
                    .append(result)
                    .append("</li>");

                html.append("<li>")
                    .append("Description: ")
                    .append(description)
                    .append("</li>");

                html.append("<li>")
                    .append("Date: ")
                    .append(date)
                    .append("</li>");

                html.append("</ul>");
            }

            html.append("</ul>");
            html.append("</body>");
            html.append("</html>");



            byte[] responseBytes = html.toString().getBytes();

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        }
    }

}