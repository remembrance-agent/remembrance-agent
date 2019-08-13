package io.p13i.ra.utils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Utilities for dealing with URIs
 */
public class URIUtils {
    /**
     * Gets a URI from a path
     *
     * @param path the string path
     * @return a URI or null if there's a syntax error
     */
    public static URI get(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
