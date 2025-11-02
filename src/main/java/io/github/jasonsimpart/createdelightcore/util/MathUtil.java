package io.github.jasonsimpart.createdelightcore.util;

public class MathUtil {
    public static int gcd(int a, int b) {
        if (a == 0)
            return b;

        return gcd(b % a, a);
    }
}
