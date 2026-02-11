package com.ci;

/**
 * The Dummy class provides simple arithmetic operations for testing purposes.
 * It includes methods for addition, subtraction, and multiplication of integers.
 */
public class Dummy {
    /**
     * A dummy method which performs addition. 
     * @param a first number
     * @param b second number
     * @return sum of a and b
    */
    public static int add(int a, int b) {
        return a + b;
    }
    /**
     * A dummy method which performs subtraction. 
     * @param a first number
     * @param b second number
     * @return difference of a and b
    */
    public static int subtract(int a, int b) {
        return a - b;
    }

    /**
     * A dummy method which performs multiplication. 
     * @param a first number
     * @param b second number
     * @return product of a and b
    */
    public static int multiply(int a, int b) {
        int product = 0;
            for (int i = 0; i < b; i++) {
                product += a;
            }
        return product;
    }
}
