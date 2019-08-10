package io.p13i.ra.utils;

public class IntegerUtils {
    public static boolean isInt(String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public static Character asCharacter(Integer i) {
        return i.toString().charAt(0);
    }
}
