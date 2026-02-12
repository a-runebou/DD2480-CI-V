package com.ci;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class DummyTest {
    /**
     * Contract:
     * The Dummy class should be loadable without throwing exceptions.
     * 
     * Expected behavior:
     * When the Dummy class is loaded, no exceptions are thrown.
     */
    @Test
    void testClassLoad() {
        new Dummy();
        assertTrue(true);
    }

    /**
     * Contract:
     * Function testMult shall return the correct product of two integers.
     * 
     * Expected behavior:
     * Given two integers a and b, the function shall return a * b.
     */
    @Test
    public void testMult() {
        int a = 5;
        int b = 4;
        int result = 20;
        int mult_result = Dummy.multiply(a, b);
        assertEquals(result, mult_result);
    }

    /**
     * Contract:
     * Function testMultWithZero shall return zero when one of the integers is zero.
     * 
     * Expected behavior:
     * Given an integer a and zero, the function shall return zero.
     */
    @Test
    public void multWithZero() {
        int a = 5;
        int b = 0;
        int result = 0;
        int mult_result = Dummy.multiply(a, b);
        assertEquals(result, mult_result);
    }
    
    /**
     * Contract:
     * Function testAdd shall return the correct sum of two integers.
     * 
     * Expected behavior:
     * Given two integers a and b, the function shall return a + b.
     */
    @Test
    public void testAdd() {
        int a = 5;
        int b = 4;
        int result = 9;
        int add_result = Dummy.add(a, b);
        assertEquals(result, add_result);
    }  
    /**
     * Contract:
     * Function testSub shall return the correct difference of two integers.
     * 
     * Expected behavior:
     * Given two integers a and b, the function shall return a - b.
     */    
    @Test
    public void testSub() {
        int a = 5;
        int b = 4;
        int result = 1;
        int sub_result = Dummy.subtract(a, b);
        assertEquals(result, sub_result);
    }
}
