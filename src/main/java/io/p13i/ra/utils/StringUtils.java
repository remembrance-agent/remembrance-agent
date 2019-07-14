package io.p13i.ra.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class StringUtils {
    public static final String NULL = "<null>";

    public static String truncateWithEllipse(String str, int maxLength) {
        if (str == null) {
            return NULL;
        }
        boolean includeEllipses = str.length() > maxLength;
        return str.substring(0, Math.min(str.length(), maxLength)) + (includeEllipses ? "..." : "");
    }

    /**
     * https://javarevisited.blogspot.com/2013/03/generate-md5-hash-in-java-string-byte-array-example-tutorial.html
     * @param message
     * @return
     */
    public static String md5(String message) {
        String digest;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes("UTF-8"));

            //converting byte array to Hexadecimal String
            StringBuilder sb = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                sb.append(String.format("%02x", b & 0xff));
            }

            digest = sb.toString();

        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return digest;
    }
}
