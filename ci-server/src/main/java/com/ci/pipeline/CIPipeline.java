package com.ci.pipeline;

import com.ci.checkout.GitCheckoutService;
import com.ci.statuses.StatusPosterAdapter;

import java.nio.file.Files;
import java.nio.file.Path;

public class CIPipeline {
    private final GitCheckoutService checkoutService;
    private final CommandRunner runner;
    private final StatusReporter statusReporter;

    /**
     * Prefer using this constructor from Server (composition root):
     * new CIPipeline(new GitCheckoutService(), new CommandRunner(), realStatusReporter)
     */
    public CIPipeline(GitCheckoutService checkoutService, CommandRunner runner, StatusReporter statusReporter) {
        this.checkoutService = checkoutService;
        this.runner = runner;
        this.statusReporter = statusReporter;
    }

    /**
     * Default constructor: uses real services for production.
     */
    public CIPipeline() {
        this(new GitCheckoutService(), new CommandRunner(), new StatusPosterAdapter());
    }

    public void run(String repoUrl, String branch, String sha) {
        System.out.println("[CI] START branch=" + branch + " sha=" + shortSha(sha));

        Path dir = null;
        try {
            safePending(sha, "CI running");

            System.out.println("[CI] CHECKOUT");
            dir = checkoutService.checkout(repoUrl, branch, sha);
            System.out.println("[CI] CHECKOUT OK dir=" + dir);

            System.out.println("[CI] TEST");

            int exit;
            Path mvnw = dir.resolve("mvnw");

            if (Files.exists(mvnw)) {
                runner.run(dir, "chmod", "+x", "mvnw");
                exit = runner.run(dir, "./mvnw", "test");
            } else {
                exit = runner.run(dir, "mvn", "test");
            }

            if (exit == 0) safeSuccess(sha, "CI passed");
            else safeFailure(sha, "CI failed (exit=" + exit + ")");

        } catch (Exception e) {
            String msg = (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage();
            System.out.println("[CI] ERROR " + msg);
            e.printStackTrace();
            safeError(sha, "CI error: " + msg);
        } finally {
            if (dir != null) {
                System.out.println("[CI] CLEANUP " + dir);
                runner.deleteRecursively(dir);
            }
            System.out.println("[CI] END branch=" + branch + " sha=" + shortSha(sha));
        }
    }

    private static String shortSha(String sha) {
        if (sha == null) return "null";
        return sha.length() < 7 ? sha : sha.substring(0, 7);
    }

    // status helpers that don’t let status posting break CI execution
    private void safePending(String sha, String msg) {
        if (statusReporter == null || sha == null) return;
        try { statusReporter.pending(sha, msg); } catch (Exception ignored) {}
    }

    private void safeSuccess(String sha, String msg) {
        if (statusReporter == null || sha == null) return;
        try { statusReporter.success(sha, msg); } catch (Exception ignored) {}
    }

    private void safeFailure(String sha, String msg) {
        if (statusReporter == null || sha == null) return;
        try { statusReporter.failure(sha, msg); } catch (Exception ignored) {}
    }

    private void safeError(String sha, String msg) {
        if (statusReporter == null || sha == null) return;
        try { statusReporter.error(sha, msg); } catch (Exception ignored) {}
    }
}

