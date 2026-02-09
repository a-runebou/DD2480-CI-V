package com.ci;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class CompileRunnerTest {

    /**
     * Contract:
     * The compile runner shall accept a valid directory without throwing
     * IllegalArgumentException at input validation time.
     *
     * Expected behavior:
     * Given a valid directory, the function shall not throw
     * IllegalArgumentException immediately.
     */
    @Test
    void validDirectoryDoesNotThrowIllegalArgumentException() {
        File currentDir = new File("..");

        assertDoesNotThrow(() -> {
            // We do not assert on the result, only that input validation passes
            CompileRunner.runMvnwCompile(currentDir);
        });
    }

    /**
     * Contract:
     * The compile runner requires a non-null repository directory.
     *
     * Expected behavior:
     * Given a null directory, the function shall throw IllegalArgumentException.
     */
    @Test
    void nullDirectoryThrowsIllegalArgumentException() {
        assertThrows(
            IllegalArgumentException.class,
            () -> CompileRunner.runMvnwCompile(null)
        );
    }

    /**
     * Contract:
     * The compile runner requires the given File to be an existing directory.
     *
     * Expected behavior:
     * Given a File that is not a directory, the function shall throw
     * IllegalArgumentException.
     */
    @Test
    void nonDirectoryFileThrowsIllegalArgumentException() {
        File notADirectory = new File("pom.xml");

        assertThrows(
            IllegalArgumentException.class,
            () -> CompileRunner.runMvnwCompile(notADirectory)
        );
    }
}
