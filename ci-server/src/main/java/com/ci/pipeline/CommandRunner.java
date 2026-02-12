package com.ci.pipeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Comparator;

/**
 * Responsible for running shell commands in the CI pipeline, such as compiling code and running tests.
 * Also provides a utility method for deleting directories recursively.
 */
public class CommandRunner {

    public int run(Path cwd, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(cwd.toFile());
        pb.redirectErrorStream(true);

        // Prevent hanging on private repos / missing credentials
        pb.environment().put("GIT_TERMINAL_PROMPT", "0");

        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int code = p.waitFor();
        System.out.println("[CI] CMD: " + String.join(" ", cmd));
        System.out.println(out);

        return code;
    }

    public void deleteRecursively(Path root) {
        try {
            if (root == null || !Files.exists(root)) return;
            Files.walk(root)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try { Files.deleteIfExists(path); }
                        catch (IOException ignored) {}
                    });
        } catch (IOException ignored) {}
    }
}
