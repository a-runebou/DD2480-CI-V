package com.ci.checkout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

public class GitCheckoutServiceTest {

    /**
     * Contract:
     * GitCheckoutService shall be instantiable without any constructor arguments.
     * 
     * Expected behavior:
     * Creating a new instance of GitCheckoutService shall not return null.
     */
    @Test
    void serviceCanBeInstantiated() {
        GitCheckoutService svc = new GitCheckoutService();
        assertNotNull(svc);
    }

    /**
     * Contract:
     * The checkout() method shall clone a Git repository into a temporary directory
     * and return the path to that directory.
     * 
     * Expected behavior:
     * Given a valid public repository URL and branch:
     * - The returned path shall not be null
     * - The returned path shall exist and be a directory
     * - The directory shall contain a .git folder (indicating successful clone)
     * - The directory shall contain expected repository files (e.g., README)
     */
    @Test
    void checkoutClonesPublicRepo() throws Exception {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();
        Path workDir = null;

        try {
            workDir = svc.checkout(
                "https://github.com/octocat/Hello-World.git",
                "master",
                null
            );

            assertNotNull(workDir, "Returned path shall not be null");
            assertTrue(Files.exists(workDir), "Work directory shall exist");
            assertTrue(Files.isDirectory(workDir), "Work directory shall be a directory");
            assertTrue(Files.exists(workDir.resolve(".git")), ".git folder shall exist");
            assertTrue(Files.exists(workDir.resolve("README")), "README shall exist");

        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Contract:
     * The checkout() method shall support checking out a specific commit SHA
     * after cloning the repository.
     * 
     * Expected behavior:
     * Given a valid repository URL, branch, and commit SHA:
     * - The returned path shall not be null
     * - The directory shall contain a .git folder
     * - The current HEAD shall match the requested commit SHA
     */
    @Test
    void checkoutSpecificCommit() throws Exception {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();
        Path workDir = null;

        try {
            String knownSha = "7fd1a60b01f91b314f59955a4e4d4e80d8edf11d";
            workDir = svc.checkout(
                "https://github.com/octocat/Hello-World.git",
                "master",
                knownSha
            );

            assertNotNull(workDir, "Returned path shall not be null");
            assertTrue(Files.exists(workDir.resolve(".git")), ".git folder shall exist");

            String currentSha = run(List.of("git", "rev-parse", "HEAD"), workDir).trim();
            assertEquals(knownSha, currentSha, "HEAD shall match the requested commit SHA");

        } finally {
            deleteRecursively(workDir);
        }
    }

    /**
     * Contract:
     * The checkout() method shall throw RuntimeException when given an invalid
     * or non-existent repository URL.
     * 
     * Expected behavior:
     * Given a non-existent repository URL, the method shall throw RuntimeException.
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
     * Contract:
     * The checkout() method shall throw RuntimeException when given an invalid
     * or non-existent branch name.
     * 
     * Expected behavior:
     * Given a valid repository URL but non-existent branch, the method shall throw RuntimeException.
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

    /**
     * Checks if Git is available on the system.
     * 
     * @return true if Git is available, false otherwise
     */
    private static boolean isGitAvailable() {
        try {
            run(List.of("git", "--version"), null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Executes a shell command and returns the output.
     * 
     * @param cmd - list of command arguments
     * @param cwd - working directory for the command (can be null)
     * @return the command output as a string
     * @throws IOException if an I/O error occurs
     * @throws InterruptedException if the process is interrupted
     * @throws RuntimeException if the command exits with non-zero status
     */
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

    /**
     * Recursively deletes a directory and all its contents.
     * Used for cleanup after tests.
     * 
     * @param root - the root directory to delete
     * @throws IOException if an I/O error occurs during deletion
     */
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
     * Contract:
     * The checkout() method shall clean up any temporary directories it creates 
     * if an error occurs during the checkout process (e.g., invalid repository URL).
     * 
     * Expected behavior:
     * Given an invalid repository URL, the method shall throw an exception and no 
     * temporary directories starting with "ci-checkout-" shall remain in the system's 
     * temp directory after the method returns.
     */
    @Test
    void checkoutShouldCleanupTempDirOnFailure() throws IOException, InterruptedException {
        Assumptions.assumeTrue(isGitAvailable(), "Git not available, skipping test");

        GitCheckoutService svc = new GitCheckoutService();

        // Get all current temp directories before the test
        Set<Path> beforeDirs = listCheckoutDirs();

        try {
            svc.checkout(
                "https://github.com/somenonexistent/repo-that-does-not-exist-lol.git",
                "main",
                null
            );
            fail("Exception ignored");
        } catch (Exception e) {}

        // Get all temp directories after the test
        Set<Path> afterDirs = listCheckoutDirs();

        // The difference should be empty
        afterDirs.removeAll(beforeDirs);
        assertTrue(afterDirs.isEmpty(), "No new temp directories should remain after failure");
    }


    private Set<Path> listCheckoutDirs() throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        try (Stream<Path> paths = Files.list(tempDir)) {
            return paths
                .filter(p -> p.getFileName().toString().startsWith("ci-checkout-"))
                .collect(Collectors.toSet());
        }
    }
}