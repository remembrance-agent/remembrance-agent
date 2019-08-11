package io.p13i.ra.utils;

/**
 * Utilities for dealing with integers
 */
public class IntegerUtils {
    /**
     * Whether or not a string is an integer
     * @param string the integer as a string
     * @return whether or not a string is an integer
     */
    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}
