package com.ci;

import com.ci.statuses.StatusPoster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.ProcessBuilder;

/**
 * The ExecuteTest class is responsible for executing the test suite and posting the results to GitHub.
 * It runs the tests using Maven, collects the results, and uses the StatusPoster to update the status on GitHub.
 */
public class ExecuteTest {
    /**
     * Executes the test suite and posts the results along with a specified URL to github.
     * @param sha the commit SHA to associate with the test results.
     * @param targetUrl the URL to post the test results to.
    */
    public static void runTests(String sha, String targetUrl) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("./mvnw", "test");
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            
            int exitCode = process.waitFor();

            StringBuilder result = new StringBuilder();
            result.append(readFile("AppTest.txt"));
            result.append(readFile("DbHandlerTest.txt"));
            result.append(readFile("DummyTest.txt"));
            result.append(readFile("ServerTest.txt"));
            result.append(readFile("statuses.StatusPosterTest.txt"));
            
            String state = (exitCode == 0) ? "success" : "failure";

            StatusPoster statusPoster = new StatusPoster();
            statusPoster.postStatus(sha, state, targetUrl, "Test execution completed with exit code: " + exitCode);
        } catch (Exception e) {
            e.printStackTrace();
            try{
                StatusPoster statusPoster = new StatusPoster();
                statusPoster.postStatus(sha, "error", targetUrl, e.getMessage());
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }


    }
    /**
     * Reads the content of a file and returns it as a string.
     * @param fileName
     * @return the content of the file as a string, or an empty string if the file is not found or an error occurs.
     */
    private static String readFile(String fileName){
    StringBuilder result = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new FileReader("target/surefire-reports/com.ci."+fileName))) {
        String line;
        while ((line = br.readLine()) != null) {
            result.append(line).append("\n");
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
    return result.toString();
    }
}
