package com.ci.statuses;

import com.ci.pipeline.StatusReporter;

/**
 * Adapter that wraps StatusPoster to implement the StatusReporter interface.
 */
public class StatusPosterAdapter implements StatusReporter {
    private final StatusPoster poster;

    public StatusPosterAdapter() {
        this.poster = new StatusPoster();
    }

    public StatusPosterAdapter(StatusPoster poster) {
        this.poster = poster;
    }

    @Override
    public void pending(String sha, String desc) throws Exception {
        poster.postStatus(sha, "pending", "", desc);
    }

    @Override
    public void success(String sha, String desc) throws Exception {
        poster.postStatus(sha, "success", "", desc);
    }

    @Override
    public void failure(String sha, String desc) throws Exception {
        poster.postStatus(sha, "failure", "", desc);
    }

    @Override
    public void error(String sha, String desc) throws Exception {
        poster.postStatus(sha, "error", "", desc);
    }
}
