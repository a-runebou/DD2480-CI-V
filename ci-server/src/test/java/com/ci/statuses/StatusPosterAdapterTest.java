package com.ci.statuses;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatusPosterAdapter.
 * Uses a fake StatusPoster to avoid real HTTP calls.
 */
public class StatusPosterAdapterTest {

    private RecordingStatusPoster recorder;
    private StatusPosterAdapter adapter;

    @BeforeEach
    void setUp() {
        recorder = new RecordingStatusPoster();
        adapter = new StatusPosterAdapter(recorder);
    }

    /**
     * When pending() is called, adapter posts "pending" state to StatusPoster.
     */
    @Test
    void pending_postsCorrectState() throws Exception {
        adapter.pending("abc123", "Build started");

        assertEquals(1, recorder.calls.size());
        assertEquals("pending", recorder.calls.get(0).state);
        assertEquals("abc123", recorder.calls.get(0).sha);
        assertEquals("Build started", recorder.calls.get(0).description);
    }

    /**
     * When success() is called, adapter posts "success" state to StatusPoster.
     */
    @Test
    void success_postsCorrectState() throws Exception {
        adapter.success("abc123", "Build passed");

        assertEquals(1, recorder.calls.size());
        assertEquals("success", recorder.calls.get(0).state);
    }

    /**
     * When failure() is called, adapter posts "failure" state to StatusPoster.
     */
    @Test
    void failure_postsCorrectState() throws Exception {
        adapter.failure("abc123", "Build failed");

        assertEquals(1, recorder.calls.size());
        assertEquals("failure", recorder.calls.get(0).state);
    }

    /**
     * When error() is called, adapter posts "error" state to StatusPoster.
     */
    @Test
    void error_postsCorrectState() throws Exception {
        adapter.error("abc123", "Build error");

        assertEquals(1, recorder.calls.size());
        assertEquals("error", recorder.calls.get(0).state);
    }


    static class RecordingStatusPoster extends StatusPoster {
        final List<PostCall> calls = new ArrayList<>();

        RecordingStatusPoster() {
            // Skip parent constructor that reads token.config
        }

        @Override
        public void postStatus(String sha, String state, String targetUrl, String description) {
            calls.add(new PostCall(sha, state, targetUrl, description));
        }

        record PostCall(String sha, String state, String targetUrl, String description) {}
    }
}
