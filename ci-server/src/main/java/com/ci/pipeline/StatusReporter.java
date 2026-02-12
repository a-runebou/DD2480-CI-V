package com.ci.pipeline;

/**
 * Abstraction for reporting CI status for a commit SHA.
 * Implementations can post to GitHub, store in DB, etc.
 */
public interface StatusReporter {
    /**
     * Posts a pending status update for the given SHA with the provided description.
     * @param sha
     * @param desc
     * @throws Exception
     */
    void pending(String sha, String desc) throws Exception;
    /**
     * Posts a success status update for the given SHA with the provided description.
     * @param sha
     * @param desc
     * @throws Exception
     */
    void success(String sha, String desc) throws Exception;
    /**
     * Posts a failure status update for the given SHA with the provided description.
     * @param sha
     * @param desc
     * @throws Exception
     */
    void failure(String sha, String desc) throws Exception;
    /**
     * Posts an error status update for the given SHA with the provided description.
     * @param sha
     * @param desc
     * @throws Exception
     */
    void error(String sha, String desc) throws Exception;
}
