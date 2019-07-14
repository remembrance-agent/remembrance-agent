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

    public static void inRange(Double value, Double lower, Double upper) {
        Assert.that(lower <= value && value <= upper, String.format("%04f not in [%04f, %04f]", value, lower, upper));
    }


    public static void inRangeOrNan(Double value, Double lower, Double upper) {
        if (Double.isNaN(value)) {
            return;
        }
        Assert.inRange(value, lower, upper);
    }
}
