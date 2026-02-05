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
}