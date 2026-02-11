package com.ci;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DummyTest {
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
