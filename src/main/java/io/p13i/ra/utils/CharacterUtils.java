package io.p13i.ra.utils;

class CharacterUtils {
    public static boolean isAlphanumeric(char c) {
        return ('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z') || ('0' <= c && c <= '9');
    }
}
