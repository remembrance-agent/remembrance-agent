package io.p13i.ra.utils;

import java.util.Objects;

/**
 * Utilities for asserting conditions in code
 */
public class Assert {

    /**
     * @param condition the condition to check
     */
    public static void that(boolean condition) {
        if (!condition) {
            fail();
        }
    }

    /**
     * @param condition the condition to check
     * @param message the message in the assertion error
     */
    public static void that(boolean condition, String message) {
        if (!condition) {
            fail(message);
        }
    }

    /**
     * @param message in the assertion error
     */
    private static void fail(String message) {
        throw new AssertionError(message);
    }

    /**
     * Fails with an assertion error
     *
     * @throws AssertionError the thrown exception
     */
    public static void fail() throws AssertionError {
        throw new AssertionError();
    }

    /**
     * Checks that a double is in a range
     *
     * @param value the value
     * @param lower the lower bound
     * @param upper the upper bound
     */
    public static void inRange(Double value, Double lower, Double upper) {
        Assert.that(lower <= value && value <= upper);
    }

    public static void equals(Object one, Object two) {
        that(Objects.equals(one, two));
    }
}
