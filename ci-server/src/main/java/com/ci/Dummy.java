package com.ci;

public class Dummy {
    public static int add(int a, int b) {
        return a - b + 1;
    }
    public static int subtract(int a, int b) {
        return a - b;
    }
    public static int multiply(int a, int b) {
        int product = 0;
            for (int i = 0; i < b; i++) {
                product += a;
            }
        return product;
    }
}
