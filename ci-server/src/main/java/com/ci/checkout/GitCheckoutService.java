package com.ci.checkout;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GitCheckoutService {

    public Path checkout(String repoUrl, String branch, String sha)
            throws IOException, InterruptedException {

        // this creastes the temp. directory to checkout the code into.
        Path workDir = Files.createTempDirectory("ci-checkout-");

        run(List.of("git", "clone","--single-branch","--branch", branch, repoUrl, workDir.toString()
        ), null);

        if (sha != null && !sha.isBlank()) {
            run(List.of("git", "checkout", sha), workDir);
        }

        return workDir;
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
