package io.p13i.ra.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Useful string utilities
 */
public class StringUtils {
    private static final String NULL = "<null>";

    /**
     * Ends a string after a given length with ...
     * @param string the string
     * @param maxLength the max length
     * @return the truncated string
     */
    public static String truncateEndWithEllipse(String string, int maxLength) {
        if (string == null) {
            return NULL;
        }
        boolean includeEllipses = string.length() > maxLength;
        return string.substring(0, Math.min(string.length(), maxLength)) + (includeEllipses ? "..." : "");
    }

    /**
     * Adds ... to the front followed by the last {@code maxLength} characters
     * @param string the string
     * @param maxLength the max length
     * @return the truncated string
     */
    public static String truncateBeginningWithEllipse(String string, int maxLength) {
        if (string == null) {
            return NULL;
        }
        boolean includeEllipses = string.length() > maxLength;
        return (includeEllipses ? "..." : "") + string.substring(Math.max(0, string.length() - maxLength));
    }

    /**
     * https://javarevisited.blogspot.com/2013/03/generate-md5-hash-in-java-string-byte-array-example-tutorial.html
     * @param message The longer string to process
     * @return The MD5 digest
     */
    public static String md5(String message) {
        String digest;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));

            //converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            digest = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return digest;
    }

    /**
     * https://stackoverflow.com/a/2560017/5071723
     * @param s the string
     * @return
     */
    public static String splitCamelCase(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])"
                ),
                " "
        );
    }

    public static Stream<Character> toCharStream(String s) {
        char[] characters = s.toCharArray();
        List<Character> list = new ArrayList<>(characters.length);
        for (char c : characters) {
            list.add(c);
        }
        return list.stream();
    }
}
