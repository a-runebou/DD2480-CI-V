package com.ci.statuses;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
