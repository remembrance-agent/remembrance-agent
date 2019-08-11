package io.p13i.ra.utils;

public class CharacterUtils {
    public static boolean isAlphanumeric(char c) {
        return isUpperCase(c) || isLowerCase(c) || isNumeric(c);
    }

    public static boolean isSpace(char c) {
        return c == ' ' || c == '␣';
    }

    public static Character toUpperCase(Character c) {
        if (!isAlphanumeric(c)) {
            return c;
        }

        return c.toString().toUpperCase().charAt(0);
    }

    public static boolean isUpperCase(char c) {
        return ('A' <= c && c <= 'Z');
    }


    public static boolean isLowerCase(char c) {
        return ('a' <= c && c <= 'z');
    }

    public static boolean isNumeric(char c) {
        return ('0' <= c && c <= '9');
    }
}
