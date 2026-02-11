package com.ci;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class CompileRunnerTest {

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
