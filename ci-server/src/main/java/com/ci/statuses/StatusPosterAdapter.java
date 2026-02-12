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

    /**
     * Posts a pending status update for the given SHA with the provided description.
     * @param sha the commit SHA to post the status for
     * @param desc the description to include with the status update
     * @throws Exception if an error occurs while posting the status
     */
    @Override
    public void pending(String sha, String desc) throws Exception {
        poster.postStatus(sha, "pending", "", desc);
    }
    /**
     * Posts a success status update for the given SHA with the provided description.
     * @param sha the commit SHA to post the status for
     * @param desc the description to include with the status update
     * @throws Exception if an error occurs while posting the status
     */
    @Override
    public void success(String sha, String desc) throws Exception {
        poster.postStatus(sha, "success", "", desc);
    }
    /**
     * Posts a failure status update for the given SHA with the provided description.
     * @param sha the commit SHA to post the status for
     * @param desc the description to include with the status update
     * @throws Exception if an error occurs while posting the status
     */
    @Override
    public void failure(String sha, String desc) throws Exception {
        poster.postStatus(sha, "failure", "", desc);
    }
    /**
     * Posts an error status update for the given SHA with the provided description.
     * @param sha the commit SHA to post the status for
     * @param desc the description to include with the status update
     * @throws Exception if an error occurs while posting the status
     */
    @Override
    public void error(String sha, String desc) throws Exception {
        poster.postStatus(sha, "error", "", desc);
    }
}
