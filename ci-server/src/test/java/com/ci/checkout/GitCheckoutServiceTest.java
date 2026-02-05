package com.ci.checkout;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GitCheckoutServiceTest {

    /**
     * Smoke test: Verify GitCheckoutService can be instantiated.
     */
    @Test
    void serviceCanBeInstantiated() {
        GitCheckoutService svc = new GitCheckoutService();
        assertNotNull(svc);
    }

    private static boolean isGitAvailable() {
        try {
            run(List.of("git", "--version"), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static String run(List<String> cmd, Path cwd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (cwd != null) pb.directory(cwd.toFile());
        pb.redirectErrorStream(true);
        pb.environment().put("GIT_TERMINAL_PROMPT", "0");

        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();

        if (code != 0) {
            throw new RuntimeException("Command failed: " + String.join(" ", cmd) + "\n" + out);
        }
        return out;
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || !Files.exists(root)) return;
        Files.walk(root)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try { Files.deleteIfExists(path); }
                    catch (IOException ignored) {}
                });
    }

    /**
     * Test: checkout() clones a public repository and creates expected files.
     * Uses a small public repo to minimize network overhead.
     */
    @Test
    void checkoutClonesPublicRepo() throws Exception {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();
        Path workDir = null;

        try {
            // Clone a small, stable public repo
            workDir = svc.checkout(
                "https://github.com/octocat/Hello-World.git",
                "master",
                null
            );

            // Verify directory was created
            assertNotNull(workDir);
            assertTrue(Files.exists(workDir), "Work directory should exist");
            assertTrue(Files.isDirectory(workDir), "Work directory should be a directory");

            // Verify .git folder exists (confirms successful clone)
            assertTrue(Files.exists(workDir.resolve(".git")), ".git folder should exist");

            // Verify README exists in Hello-World repo
            assertTrue(Files.exists(workDir.resolve("README")), "README should exist");

        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Test: checkout() can checkout a specific commit SHA.
     */
    @Test
    void checkoutSpecificCommit() throws Exception {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();
        Path workDir = null;

        try {
            // Clone and checkout a specific known commit from Hello-World
            String knownSha = "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d";
            workDir = svc.checkout(
                "https://github.com/octocat/Hello-World.git",
                "master",
                knownSha
            );

            // Verify checkout succeeded
            assertNotNull(workDir);
            assertTrue(Files.exists(workDir.resolve(".git")), ".git folder should exist");

            // Verify we're at the correct commit
            String currentSha = run(List.of("git", "rev-parse", "HEAD"), workDir).trim();
            assertEquals(knownSha, currentSha, "Should be at the specified commit");

        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Test: checkout() throws exception for invalid repository URL.
     */
    @Test
    void checkoutInvalidRepoThrows() {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();

        assertThrows(RuntimeException.class, () -> {
            svc.checkout(
                "https://github.com/somenonexistent/repo-that-does-not-exist-lol.git",
                "main",
                null
            );
        });
    }

    /**
     * Test: checkout() throws exception for invalid branch.
     */
    @Test
    void checkoutInvalidBranchThrows() {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();

        assertThrows(RuntimeException.class, () -> {
            svc.checkout(
                "https://github.com/octocat/Hello-World.git",
                "nonexistent-random;branch-lol",
                null
            );
        });
    }
}