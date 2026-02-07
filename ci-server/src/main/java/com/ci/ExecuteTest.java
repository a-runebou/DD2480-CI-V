package com.ci;

import com.ci.statuses.StatusPoster;
import java.lang.ProcessBuilder;

public class ExecuteTest {
    public static void runTests(String sha, String targetUrl) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("./mvnw", "test", "-Dtest=!ServerTest");
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            String state = (exitCode == 0) ? "success" : "failure";

            StatusPoster statusPoster = new StatusPoster();
            statusPoster.postStatus(sha, state, targetUrl, "Build and test execution completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
