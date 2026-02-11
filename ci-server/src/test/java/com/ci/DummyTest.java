package com.ci;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class DummyTest {
    @Test
    public void testMult() {
        int a = 5;
        int b = 4;
        int result = 20;
        int mult_result = Dummy.multiply(a, b);
        assertEquals(result, mult_result);
    }
    
    @Test
    public void testAdd() {
        int a = 5;
        int b = 4;
        int result = 9;
        int add_result = Dummy.add(a, b);
        assertEquals(result, add_result);
    }  
    
    @Test
    public void testSub() {
        int a = 5;
        int b = 4;
        int result = 1;
        int sub_result = Dummy.subtract(a, b);
        assertEquals(result, sub_result);
    }
}
