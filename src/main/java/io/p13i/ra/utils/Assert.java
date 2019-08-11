package io.p13i.ra.utils;

/**
 * Utilities for asserting conditions in code
 */
public class Assert {

    /**
     * @param condition the condition to check
     * @param message the message to print if the condition is not met
     */
    public static void that(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * Fails with an assertion error
     * @param message the message of the exception
     * @throws AssertionError the thrown exception
     */
    public static void fail(String message) throws AssertionError {
        throw new AssertionError(message);
    }

    /**
     * Checks if Doubles are equal-ish
     * @param d1 the first one
     * @param d2 the second one
     */
    public static void almostEqual(Double d1, Double d2) {
        final double TOLERANCE = 0.01;
        that(Math.abs(d1 - d2) < TOLERANCE, "Doubles are not almost almostEqual within tolerance of " + TOLERANCE);
    }

    /**
     * Checks that a double is in a range
     * @param value the value
     * @param lower the lower bound
     * @param upper the upper bound
     */
    public static void inRange(Double value, Double lower, Double upper) {
        Assert.that(lower <= value && value <= upper, String.format("%04f not in [%04f, %04f]", value, lower, upper));
    }
}
