package io.p13i.ra.utils;

public class Assert {
    public static void that(boolean condition) {
        that(condition, "Assertion rejected");
    }

    public static void that(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    public static void fail(String message) {
        throw new AssertionError(message);
    }

    public static void equal(Double d1, Double d2) {
        final double TOLERANCE = 0.01;
        that((d1 - d2) < TOLERANCE);
    }
}
