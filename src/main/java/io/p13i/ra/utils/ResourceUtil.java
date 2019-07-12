package io.p13i.ra.utils;

import java.io.InputStream;

public class ResourceUtil {
    public static <T> String getResourcePath(Class<T> relativeToClass, String resourceName) {
        return relativeToClass.getClassLoader().getResource(resourceName).getPath();
    }

    public static <T> InputStream getResourceStream(Class<T> relativeToClass, String resourceName) {
        return relativeToClass.getClassLoader().getResourceAsStream(resourceName);
    }
}
