package com.ci.pipeline;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class CommandRunner {

    /**
     * Run a command in a working directory and return the exit code.
     * Stdout/stderr are merged and printed.
     */
    public int run(Path cwd, String... cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(cwd.toFile());
        pb.redirectErrorStream(true);

        Process p = pb.start();
        String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        int code = p.waitFor();
        System.out.println("[CI] CMD: " + String.join(" ", cmd));
        System.out.println(out);

        return code;
    }
}
