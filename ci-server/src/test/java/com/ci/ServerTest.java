package com.ci;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;


public class ServerTest {

    
    private Server server;
    private HttpServer httpServer;
    private int port;
    private static String VALID_PAYLOAD = """
        {
            "ref": "refs/heads/main",
            "after": "abc123",
            "repository": {
                "clone_url": "https://github.com/test/repo.git"
            }
        }
        """;
    private static String INVALID_PAYLOAD = "{ \"reff\": \"refs/heads/main\", \"after\": \"abc123\" }";

    @BeforeEach
    public void setUp() throws Exception {
        server = new Server();
        server.start();
        port = 2480 + 5;
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    /**
     * Contract:
     * When a POST request is sent to /webhook, with a JSON payload matching the expected 
     * GitHub webhook format (refs, after), the server should respond with 200 OK.
     * 
     * Expected Behavior:
     * The server processes the POST request to /webhook and returns a 200 OK response.
     */
    @Test
    public void testWebhookPostReturns200() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(VALID_PAYLOAD.getBytes());
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
    }


    /**
     * Contract:
     * When a POST request is sent to /webhook with an invalid JSON payload,
     * the server should respond with 400 Bad Request.
     * 
     * Expected Behavior:
     * The server rejects the invalid payload and returns a 400 response.
     */
    @Test
    public void testWebhookPostWithInvalidPayloadReturns400() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(INVALID_PAYLOAD.getBytes());
        int responseCode = connection.getResponseCode();
        assertEquals(400, responseCode);
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

    /**
     * Contract:
     * When a POST request is sent to /webhook with an empty body,
     * the server shall respond with 400 Bad Request.
     * 
     * Expected behavior:
     * The server rejects the request with no payload and returns a 400 response.
     */
    @Test
    public void testEmptyBodyReturns400() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        connection.getOutputStream().close();

        assertEquals(400, connection.getResponseCode());
    }

    /**
     * Contract:
     * When a POST request is sent to /webhook with a JSON payload that is missing
     * the required repository.clone_url field, the server shall respond with 400 Bad Request.
     * 
     * Expected behavior:
     * The server validates the payload structure and rejects requests missing clone_url,
     * returning a 400 response.
     */
    @Test
    public void testMissingCloneUrlReturns400() throws Exception {
        String payload = """
            {"ref":"refs/heads/main","after":"abc123","repository":{}}
            """;

        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        connection.getOutputStream().write(payload.getBytes());
        connection.getOutputStream().close();

        assertEquals(400, connection.getResponseCode());
    }


}