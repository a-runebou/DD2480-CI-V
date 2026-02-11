package com.ci.pipeline;

import com.ci.checkout.GitCheckoutService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CIPipeline using fake implementations to avoid real git clones and Maven runs.
 */
public class CIPipelineTest {

    private RecordingStatusReporter statusReporter;
    private FakeCommandRunner commandRunner;
    private Path tempDir;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        this.tempDir = tempDir;
        this.statusReporter = new RecordingStatusReporter();
        this.commandRunner = new FakeCommandRunner();
    }

    /**
     * When tests pass (exit code 0), pipeline posts pending to success.
     */
    @Test
    void successPath_postsPendingThenSuccess() {
        FakeGitCheckoutService checkoutService = new FakeGitCheckoutService(tempDir);
        commandRunner.setExitCode(0);
        CIPipeline pipeline = new CIPipeline(checkoutService, commandRunner, statusReporter);

        pipeline.run("https://github.com/test/repo.git", "main", "abc1234");

        assertEquals(2, statusReporter.statuses.size());
        assertEquals("pending", statusReporter.statuses.get(0).state);
        assertEquals("success", statusReporter.statuses.get(1).state);
    }

    /**
     * When tests fail (non-zero exit code), pipeline posts pending to failure.
     */
    @Test
    void failurePath_postsPendingThenFailure() {
        FakeGitCheckoutService checkoutService = new FakeGitCheckoutService(tempDir);
        commandRunner.setExitCode(1);
        CIPipeline pipeline = new CIPipeline(checkoutService, commandRunner, statusReporter);

        pipeline.run("https://github.com/test/repo.git", "main", "abc1234");

        assertEquals(2, statusReporter.statuses.size());
        assertEquals("pending", statusReporter.statuses.get(0).state);
        assertEquals("failure", statusReporter.statuses.get(1).state);
    }

    /**
     * When checkout throws an exception, pipeline posts pending to error.
     */
    @Test
    void exceptionPath_postsError() {
        FailingGitCheckoutService checkoutService = new FailingGitCheckoutService();
        CIPipeline pipeline = new CIPipeline(checkoutService, commandRunner, statusReporter);

        pipeline.run("https://github.com/test/repo.git", "main", "abc1234");

        assertEquals(2, statusReporter.statuses.size());
        assertEquals("pending", statusReporter.statuses.get(0).state);
        assertEquals("error", statusReporter.statuses.get(1).state);
    }

    static class RecordingStatusReporter implements StatusReporter {
        final List<StatusUpdate> statuses = new ArrayList<>();

        @Override public void pending(String sha, String desc) { statuses.add(new StatusUpdate("pending", sha, desc)); }
        @Override public void success(String sha, String desc) { statuses.add(new StatusUpdate("success", sha, desc)); }
        @Override public void failure(String sha, String desc) { statuses.add(new StatusUpdate("failure", sha, desc)); }
        @Override public void error(String sha, String desc) { statuses.add(new StatusUpdate("error", sha, desc)); }

        record StatusUpdate(String state, String sha, String description) {}
    }

    static class FakeGitCheckoutService extends GitCheckoutService {
        private final Path fakeDir;

        FakeGitCheckoutService(Path fakeDir) { this.fakeDir = fakeDir; }

        @Override
        public Path checkout(String repoUrl, String branch, String sha) {
            try { Files.createFile(fakeDir.resolve("mvnw")); } catch (IOException ignored) {}
            return fakeDir;
        }
    }

    static class FailingGitCheckoutService extends GitCheckoutService {
        @Override
        public Path checkout(String repoUrl, String branch, String sha) throws IOException {
            throw new IOException("Simulated checkout failure");
        }
    }

    static class FakeCommandRunner extends CommandRunner {
        private int exitCode = 0;

        void setExitCode(int exitCode) { this.exitCode = exitCode; }

        @Override public int run(Path cwd, String... cmd) { return exitCode; }
        @Override public void deleteRecursively(Path root) {}
    }
}
