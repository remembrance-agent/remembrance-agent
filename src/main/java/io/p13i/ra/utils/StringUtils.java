package io.p13i.ra.utils;

public class StringUtils {
    public static String truncateWithEllipse(String str, int maxLength) {
        boolean includeEllipses = str.length() > maxLength;
        return str.substring(0, Math.min(str.length(), maxLength)) + (includeEllipses ? "..." : "");
    }
}
