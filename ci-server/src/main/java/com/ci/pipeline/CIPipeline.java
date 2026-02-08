package com.ci.pipeline;

import com.ci.checkout.GitCheckoutService;

import java.nio.file.Path;

public class CIPipeline {
    private final GitCheckoutService checkoutService;
    private final CommandRunner runner;
    private final StatusReporter statusReporter;

    public CIPipeline(GitCheckoutService checkoutService, CommandRunner runner, StatusReporter statusReporter) {
        this.checkoutService = checkoutService;
        this.runner = runner;
        this.statusReporter = statusReporter;
    }

    public CIPipeline(StatusReporter statusReporter) {
        this(new GitCheckoutService(), new CommandRunner(), statusReporter);
    }

    public void run(String repoUrl, String branch, String sha) {
        System.out.println("[CI] START branch=" + branch + " sha=" + shortSha(sha));

        Path dir = null;
        try {
            safePending(sha, "CI running");
            dir = checkoutService.checkout(repoUrl, branch, sha);

            int exit = runner.run(dir, "mvn", "test");
            if (exit == 0) safeSuccess(sha, "CI passed");
            else safeFailure(sha, "CI failed (exit=" + exit + ")");

        } catch (Exception e) {
            System.out.println("[CI] ERROR " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("[CI] END branch=" + branch + " sha=" + shortSha(sha));
        }
    }

    private static String shortSha(String sha) {
        if (sha == null) return "null";
        return sha.length() < 7 ? sha : sha.substring(0, 7);
    }

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
}
