package io.p13i.ra.utils;

/**
 * Utilities for dealing with characters
 */
public class CharacterUtils {
    /**
     * @param c the character to check
     * @return whether the character is alphanumeric
     */
    public static boolean isAlphanumeric(char c) {
        return isUpperCase(c) || isLowerCase(c) || isNumeric(c);
    }

    /**
     * @param c the character to check
     * @return whether or not the character is a space
     */
    public static boolean isSpace(char c) {
        return c == ' ' || c == '␣';
    }

    /**
     * Converts a character to lower-case
     * @param c the character to convert
     * @return a lower-case version
     */
    public static Character toUpperCase(Character c) {
        if (!isAlphanumeric(c)) {
            return c;
        }

        return c.toString().toUpperCase().charAt(0);
    }

    /**
     * @param c the character to check
     * @return whether or not the character is upper-case
     */
    public static boolean isUpperCase(char c) {
        return ('A' <= c && c <= 'Z');
    }


    /**
     * @param c the character to check
     * @return whether or not the character is lower-case
     */
    public static boolean isLowerCase(char c) {
        return ('a' <= c && c <= 'z');
    }

    /**
     * @param c the character to check
     * @return whether or not the character is numeric
     */
    public static boolean isNumeric(char c) {
        return ('0' <= c && c <= '9');
    }
}
