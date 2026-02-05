package com.ci;
import java.lang.ProcessBuilder;

public class ExecuteTest {
    public static void runTests() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("./mvnw", "test");
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.out.println("Tests failed with exit code: " + exitCode);
            } else {
                System.out.println("Tests executed successfully.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
