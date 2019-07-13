package io.p13i.ra.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtils {
    public static URI get(String path) {
        try {
            return new URI(path);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }
}
