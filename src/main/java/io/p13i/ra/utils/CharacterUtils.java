package io.p13i.ra.utils;

public class CharacterUtils {
    public static boolean isAlphanumeric(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9');
    }

    public static Character toUpperCase(Character c) {
        return c.toString().toUpperCase().charAt(0);
    }
}
