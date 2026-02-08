package com.ci.rest;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ci.DbHandler;
import com.ci.Server;

public class AllBuildsHandlerTest {
    private Server server;
    private int port;
    private File tempDbFile;
    private String dbUrl;

    @BeforeEach
    public void setUp() throws Exception {
        tempDbFile = Files.createTempFile("testdb", ".db").toFile();
        dbUrl = "jdbc:sqlite:" + tempDbFile.getAbsolutePath();
        // Initialize the database with test data 
        DbHandler dbHandler = new DbHandler(dbUrl);
        dbHandler.createBuildTable();
        dbHandler.addEntry("1a24", "branch1", "pending");
        dbHandler.addEntry("1a25", "branch1", "success");
        dbHandler.addEntry("1a26", "branch3", "error");
        server = new Server(dbUrl);
        server.start();
        port = 2480 + 5;
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (server != null) {
            server.stop();
        }
        if (tempDbFile.exists()) {
            tempDbFile.delete();
        }
    }

    /**
     * Contract:
     * The AllBuildsHandler should only allow GET requests.
     * 
     * Expected Behavior:
     * When a POST request is sent to /builds, the server should respond with a 405 Method Not Allowed status code.
     */
    @Test
    void postRequestNotAllowed() throws Exception {
        URL url = new URL("http://localhost:" + port + "/builds");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        int responseCode = connection.getResponseCode();
        assertEquals(405, responseCode);
    }

    /**
     * Contract:
     * When a GET request is sent to /builds, the server should respond with a 200 OK status 
     * code and a JSON array of all build entries.
     * 
     * Expected Behavior:
     * The server processes the GET request to /builds and returns a 200 OK response with a JSON array containing all build entries.
     */
    @Test
    void getRequestReturns200() throws Exception {
        URL url = new URL("http://localhost:" + port + "/builds");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        assertEquals(200, responseCode);
        // get the response body and check that it contains the expected builds
        String responseBody = new String(connection.getInputStream().readAllBytes());
        assert(responseBody.contains("1a24"));
        assert(responseBody.contains("1a25"));
        assert(responseBody.contains("1a26"));
    }
}
