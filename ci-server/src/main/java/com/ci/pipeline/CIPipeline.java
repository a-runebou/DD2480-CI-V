package com.ci.pipeline;

import java.nio.file.Files;
import java.nio.file.Path;

import com.ci.DbHandler;
import com.ci.checkout.GitCheckoutService;
import com.ci.statuses.StatusPosterAdapter;

public class CIPipeline {
    private final GitCheckoutService checkoutService;
    private final CommandRunner runner;
    private final StatusReporter statusReporter;

    private static final String CI_CONTEXT = "ci-server";
    private static final String TOKEN_CONFIG_SRC = "../token.config";
    private static final String TOKEN_CONFIG_DEST = "ci-server/src/main/resources/token.config";

    private DbHandler dbHandler;
    /**
     * Prefer using this constructor from Server (composition root):
     * new CIPipeline(new GitCheckoutService(), new CommandRunner(), realStatusReporter)
     */
    public CIPipeline(GitCheckoutService checkoutService, CommandRunner runner, StatusReporter statusReporter) {
        this.checkoutService = checkoutService;
        this.runner = runner;
        this.statusReporter = statusReporter;
        this.dbHandler = new DbHandler();
    }

    /**
     * Default constructor: uses real services for production.
     */
    public CIPipeline() {
        this(new GitCheckoutService(), new CommandRunner(), new StatusPosterAdapter());
    }


    /**
     * Sets the database handler to use for recording build statuses.
     * 
     * This is primarily intended for testing, where a mock database can be injected.
     * 
     * @param dbHandler the database handler to use
     */
    public void setDbHandler(DbHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void run(String repoUrl, String branch, String sha) {
        System.out.println("[CI] START branch=" + branch + " sha=" + shortSha(sha));

        Path dir = null;
        try {
            safePending(sha, "CI running");
            dbHandler.addEntry(sha, branch, "pending");
            System.out.println("[CI] CHECKOUT");
            dir = checkoutService.checkout(repoUrl, branch, sha);
            System.out.println("[CI] CHECKOUT OK dir=" + dir);

            System.out.println("[CI] TEST");

            // TODO: Make this dynamic
            // Copy token.config into the repo directory for tests that need it
            Path source = Path.of(TOKEN_CONFIG_SRC).toAbsolutePath();
            Path dest = dir.resolve(TOKEN_CONFIG_DEST);
            Files.createDirectories(dest.getParent());
            runner.run(dir, "cp", source.toString(), dest.toString());

            // Change directory 
            dir = dir.resolve(CI_CONTEXT);

            int exit;
            Path mvnw = dir.resolve("mvnw");
            CommandRunner.TestResult result;
            if (Files.exists(mvnw)) {
                runner.run(dir, "chmod", "+x", "mvnw");
                result = runner.run(dir, "./mvnw", "test");
            } else {
                result = runner.run(dir, "mvn", "test");
            }
            exit = result.exitCode();
            if (exit == 0) {
                safeSuccess(sha, "CI passed");
                dbHandler.updateEntry(sha, branch, "success", result.logs());
            } else {
                safeFailure(sha, "CI failed (exit=" + exit + ")");
                dbHandler.updateEntry(sha, branch, "failure", result.logs());
            }

        } catch (Exception e) {
            String msg = (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage();
            System.out.println("[CI] ERROR " + msg);
            e.printStackTrace();
            safeError(sha, "CI error: " + msg);
            dbHandler.updateEntry(sha, branch, "error", "Error during CI: " + msg);
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

