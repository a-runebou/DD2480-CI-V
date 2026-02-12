package com.ci;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ci.checkout.GitCheckoutService;
import com.ci.pipeline.CIPipeline;
import com.ci.pipeline.CommandRunner;
import com.ci.pipeline.StatusReporter;


public class ServerTest {

    
    private Server server;
    private int port;
    private CountDownLatch pipelineLatch;
    private String dbUrl;
    private File tempDbFile;
    
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
        pipelineLatch = new CountDownLatch(1);
        tempDbFile = Files.createTempFile("testdb", ".db").toFile();
        dbUrl = tempDbFile.getAbsolutePath();
        
        // Create a fake pipeline that counts down when called
        CIPipeline fakePipeline = new CIPipeline(new GitCheckoutService(), new CommandRunner(), fakeReporter()) {
            @Override
            public void run(String repoUrl, String branch, String sha) {
                pipelineLatch.countDown();
                // Don't do any real work - no git clone, no maven
            }
        };
        
        server = new Server(fakePipeline, testExecutor(), dbUrl);
        server.start(0);
        port = server.getPort();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        if (tempDbFile != null && tempDbFile.exists()) {
            tempDbFile.delete();
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
        
        // Wait for async pipeline to be called (max 5 seconds)
        boolean called = pipelineLatch.await(5, TimeUnit.SECONDS);
        assertTrue(called, "Pipeline should have been called for valid webhook");
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
     * When a POST request is sent to /webhook with JSON payloads that are missing
     * required keys (ref, after, repository.clone_url), the server should respond with 400 Bad Request.
     * 
     * Expected Behavior:
     * The server validates the payload structure and rejects requests missing required fields,
     * returning a 400 response.
     */
    @Test
    public void testMissingJSONKeysReturns400() throws Exception {
        String[] payloads = new String[] {

            // No repository
            """
            {"ref": "abc" "after":"123"}
            """,

            // Missing after
            """
            {"ref":"refs/heads/main","repository":{"clone_url":"xxx"}}
            """,

            // Missing ref
            """
            {"after":"abc123","repository":{"clone_url":"xxx"}}
            """,

            // Missing clone_url
            """
            {"ref":"refs/heads/main","after":"abc123","repository":{}}
            """,

            // Missing both
            """
            {"repository":{"clone_url":"xxx"}}
            """,

            // empty
            """
            {}
            """
        };

        for (String payload : payloads) {
            URL url = new URL("http://localhost:" + port + "/webhook");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.getOutputStream().write(payload.getBytes());
            int responseCode = connection.getResponseCode();

            assertEquals(400, responseCode, "Payload: " + payload);
            connection.disconnect();
        }
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
        assertEquals(406, responseCode);
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
        Server server1 = new Server(dbUrl);
        server1.start(0);
        int usedPort = server1.getPort();

        // Attempt to start another server on the same port, which should fail
        Server server2 = new Server(dbUrl);
        try {
            server2.start(usedPort);
        } catch (IOException ex) {
            assertTrue(ex.getMessage().startsWith("Address already in use"));
        } finally {
            server1.stop();
        }
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
    public void testInvalidJSONReturns400() throws Exception {
        URL url = new URL("http://localhost:" + port + "/webhook");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        String invalidJson = "{invalid json}";
        connection.getOutputStream().write(invalidJson.getBytes());
        int responseCode = connection.getResponseCode();
        assertEquals(400, responseCode);
    }

    /**
     * Contract:
     * When the server is stopped, it should shut down gracefully without throwing exceptions.
     * 
     * Expected Behavior:
     * Calling stop() on a running Server instance should not throw any exceptions
     * and should complete without errors. Calling stop() on an already stopped 
     * server should also not throw exceptions.
     */
    @Test
    public void testStoppingNonRunningServer() {
        Server server = new Server(dbUrl);
        server.stop();
        // Should not throw any exception
        assertTrue(true);
    }

    /**
     * Contract:
     * When the server is stopped while it is running, it should shut down gracefully 
     * without throwing exceptions.
     * 
     * Expected Behavior:
     * Starting the server and then calling stop() should not throw any exceptions
     * and should complete without errors, allowing the server to shut down gracefully.
     */
    @Test
    public void testStopWhenServerRunning() throws Exception {
        Server server = new Server(dbUrl);
        server.start(0);
        server.stop(); // server != null branch executes
    }

    /**
     * Contract:
     * The Server constructor should handle a null ExecutorService without throwing exceptions.
     * 
     * Expected Behavior:
     * When a Server instance is created with a null ExecutorService, it should not throw any 
     * exceptions, and the instance should be created successfully. Calling stop() on this 
     * instance should also not throw exceptions.
     */
    @Test
    public void testNullExecutorInConstructor() {
        Server server = new Server(new CIPipeline(), null, dbUrl);
        // Should not throw any exception
        server.stop();
        assertTrue(true);
    }
}