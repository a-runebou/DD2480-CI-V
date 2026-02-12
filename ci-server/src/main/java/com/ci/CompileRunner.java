package com.ci;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The CompileRunner class is responsible for executing the Maven compile command in a given repository directory.
 * It captures the output and error messages from the process and returns them along with the exit code.
 */
public class CompileRunner {

    /**
     * Result object holding the outcome of a Maven compile run.
     */

    public static final class Result {
        public final int exitCode;          // Process exit code
        public final String outputMessage;  // Standard output from Maven
        public final String errorMessage;   // Error output from Maven

        public Result(int exitCode, String outputMessage, String errorMessage) {
            this.exitCode = exitCode;
            this.outputMessage = outputMessage;
            this.errorMessage = errorMessage;
        }
    }
    /**
     * Executes "./mvnw compile" in the given repository directory.
     *
     * @param repoDir directory containing the Maven project
     * @return Result containing exit code and process output
     */

    public static Result runMvnwCompile(File repoDir)
            throws IOException, InterruptedException {

        if (repoDir == null || !repoDir.isDirectory()) {
            throw new IllegalArgumentException("repoDir must be an existing directory");
        }

        ProcessBuilder pb = new ProcessBuilder(getMvnwCommand(repoDir), "compile");
        pb.directory(repoDir);
        Process process = pb.start();

        // Read standard output and error output from the process
        String outputMessage = readAll(process.getInputStream());
        String errorMessage = readAll(process.getErrorStream());

        int exitCode = process.waitFor();

        return new Result(exitCode, outputMessage, errorMessage);
    }
    // Get the correct Maven command depending on operating system
    private static String getMvnwCommand(File repoDir) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        if (!isWindows) {
            return "./mvnw";
        } else {
            return "mvnw.cmd";
        }
    }

    /**
     * Reads all text from an InputStream and returns it as a String.
     */
    private static String readAll(java.io.InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        // Wrap InputStream to read text line by line
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }
}
