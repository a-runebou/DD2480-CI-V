package com.ci.statuses;

import javax.naming.InsufficientResourcesException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class StatusPosterTest {

    /**
     * Contract:
     * Given a valid file token.config, the StatusPoster shall initialize without throwing an exception.
     */
    @Test
    void testConstructorDoesNotThrow() {
        assertDoesNotThrow(() -> {
            StatusPoster sp = new StatusPoster();
        });
    }


    /**
     * Contract:
     * The GitHub API accepts only error, failure, pending, or success values for the state.
     * 
     * Expected behavior:
     * Given any other value for the state, the function shall throw IllegalArgumentException.
     * @throws InsufficientResourcesException 
     */
    @Test
    void invalidStateThrowsIllegalArgumentException() {
        StatusPoster sp = new StatusPoster();
        assertThrows(
            IllegalArgumentException.class,
            () -> sp.postStatus("xxx", "testState", "testUrl", "test description")
        );
    }


    /**
     * Contract:
     * The postStatus function shall post a new commit status to the github API, and receive a response
     * with status code in the range of 200-299 if everything went correctly, or different if an error occured. If status
     * code is different, the function shall throw RuntimeException.
     * 
     * Expected behavior:
     * Given wrong SHA of a non-existing commit, the function shall throw RuntimeException.
     */
    @Test
    void wrongShaResultsInRuntimeException() {
        StatusPoster sp = new StatusPoster();
        assertThrows(RuntimeException.class, () -> {
            sp.postStatus(null, "success",
             "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ&start_radio=1", "test description");
        });
    }

    /**
     * Contract:
     * The postStatus function shall post a new commit status to the github API, and receive a response
     * with status code in the range of 200-299 if everything went correctly, or different if an error occured. If status
     * code is different, the function shall throw RuntimeException. The function shall be injection safe.
     * 
     * Expected behavior:
     * Given a valid SHA of an existing commit, the function shall complete without throwing exceptions.
     */
    @Test
    void validShaDoesNotThrow() {
        StatusPoster sp = new StatusPoster();
        assertDoesNotThrow(()-> {
            sp.postStatus("7f37d2da1b409a2ce60b8a068d081745372f7f39", "success", // test commit on the issue/8-test-branch
            "https://www.youtube.com/watch?v=dQw4w9WgXcQ&list=RDdQw4w9WgXcQ&start_radio=1", "this is a test\" description with ', /, \n aa and \\ characters"); 
        });
    }
}
