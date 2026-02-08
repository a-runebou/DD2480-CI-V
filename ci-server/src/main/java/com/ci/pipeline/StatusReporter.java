package com.ci.pipeline;

/**
 * Abstraction for reporting CI status for a commit SHA.
 * Implementations can post to GitHub, store in DB, etc.
 */
public interface StatusReporter {
    void pending(String sha, String desc) throws Exception;
    void success(String sha, String desc) throws Exception;
    void failure(String sha, String desc) throws Exception;
    void error(String sha, String desc) throws Exception;
}
