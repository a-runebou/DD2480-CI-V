package com.ci.checkout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Responsible for checking out code, cloning the repository, and checking out the specific commit.
 * Creates a temporary directory for the checkout and ensures cleanup in case of failure.
 */
public class GitCheckoutService {

    /**
     * Checks out the code from the specified repository URL, branch, and commit SHA.
     * @param repoUrl
     * @param branch
     * @param sha
     * @return
     * @throws IOException
     * @throws InterruptedException
     */
    public Path checkout(String repoUrl, String branch, String sha)
            throws IOException, InterruptedException {

        // this creastes the temp. directory to checkout the code into.
        Path workDir = Files.createTempDirectory("ci-checkout-");

        try {
            run(List.of(
                "git", "clone","--single-branch",
                "--branch", branch, repoUrl, workDir.toString()
            ), null);

            if (sha != null && !sha.isBlank()) {
                run(List.of("git", "checkout", sha), workDir);
            }

            return workDir;
        } catch (Exception e) {
            // Cleanup on failure
            Files.walk(workDir)
                .sorted(Comparator.reverseOrder()) // Delete children first
                .forEach(p -> {
                    try { 
                        Files.deleteIfExists(p); 
                    } catch (IOException ex) {}
                });
            throw e;
        }
    }

    private static String run(List<String> cmd, Path cwd)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (cwd != null) pb.directory(cwd.toFile());

        pb.redirectErrorStream(true);
        pb.environment().put("GIT_TERMINAL_PROMPT", "0"); // never prompt in CI

        Process p = pb.start();
        String output = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();

        if (code != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", cmd) + "\n" + output);
        }
        return output;
    }
}
