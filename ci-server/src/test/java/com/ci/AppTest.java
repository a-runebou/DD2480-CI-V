package com.ci;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;


public class AppTest {
    @Test
    public void testApp() {
        App app = new App();
        assertTrue(app != null);
    }

    @Test
    public void testMainRuns() {
        App.main(new String[]{});
    }
}
