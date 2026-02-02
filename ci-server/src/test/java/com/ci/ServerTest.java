package com.ci;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;


public class ServerTest {

    
    private Server server;
    private HttpServer httpServer;
    private int port;

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server();
        server.start();
        port = 1111;
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Contract:
     * When a POST request is sent to /webhook, the server should respond with 200 OK.
     * 
     * Expected Behavior:
     * The server processes the POST request to /webhook and returns a 200 OK response.
     */
    @Test
    public void testWebhookPostReturns200() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }

    /**
     * Contract:
     * When a non-POST request is sent to /webhook, the server should respond
     * with 405 Method Not Allowed.
     * 
     * Expected Behavior:
     * The server rejects the non-POST request to /webhook and returns a 405 response.
     */
    @Test
    public void testInvalidRequestTypeReturns405() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        assertEquals(405, responseCode);
    }


    /**
     * Contract:
     * When a request is sent to an unknown route, the server should respond with 404 Not Found.
     * 
     * Expected Behavior:
     * The server returns a 404 Not Found response for requests to /unknown.
     */
    @Test
    public void testUnknownRouteReturns404() throws Exception {
        URL url = new URL("http://localhost:" + port + "/unknown");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        int responseCode = connection.getResponseCode();
        assertEquals(404, responseCode);
    }
}
