package com.ci;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ci.pipeline.CIPipeline;
import com.ci.pipeline.StatusReporter;


public class ServerTest {

    
    private Server server;
    private int port;
    private AtomicBoolean pipelineCalled;
    
    private static final String VALID_PAYLOAD = """
        {
            "ref": "refs/heads/main",
            "after": "abc123",
            "repository": {
                "clone_url": "https://github.com/test/repo.git"
            }
        }
        """;
    private static final String INVALID_PAYLOAD = """
    {
        "reff": "refs/heads/main",
        "after": "abc123",
        "repository": {
            "clone_url": "https://github.com/test/repo.git"
        }
    }
    """;

    /**
     * Creates a fake StatusReporter that does nothing.
     */
    private static StatusReporter fakeReporter() {
        return new StatusReporter() {
            @Override public void pending(String sha, String desc) {}
            @Override public void success(String sha, String desc) {}
            @Override public void failure(String sha, String desc) {}
            @Override public void error(String sha, String desc) {}
        };
    }

    /**
     * Creates a single-thread executor with daemon threads for tests.
     * Daemon threads won't prevent JVM shutdown between tests.
     */
    private static ExecutorService testExecutor() {
        return Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @BeforeEach
    public void setUp() throws Exception {
        pipelineCalled = new AtomicBoolean(false);
        
        // Create a fake pipeline that just records it was called
        CIPipeline fakePipeline = new CIPipeline(fakeReporter()) {
            @Override
            public void run(String repoUrl, String branch, String sha) {
                pipelineCalled.set(true);
                // Don't do any real work - no git clone, no maven
            }
        };
        
        server = new Server();
        server.start(0);
        port = server.getPort();
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
     * The pipeline should be called with the extracted parameters.
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
        assertTrue(pipelineCalled.get(), "Pipeline should have been called for valid webhook");
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
    public void testWebhookPostMissingRefReturns400() throws Exception {
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


    /**
     * Contract:
     * When the server is started on a port that is already in use, it should throw an IOException.
     * 
     * Expected Behavior:
     * The server fails to start and throws an IOException with the message "Address already in use" 
     * when attempting to bind to a port that is already occupied by another server instance.
     */
    @Test
    public void testStartOnUnavailablePortThrowsException() throws Exception {
        // Start a server on an available port
        Server server1 = new Server();
        server1.start(0);
        int usedPort = server1.getPort();

        // Attempt to start another server on the same port, which should fail
        Server server2 = new Server();
        try {
            server2.start(usedPort);
        } catch (IOException ex) {
            assertEquals("Address already in use", ex.getMessage());
        } finally {
            server1.stop();
        }
    }
}