package io.p13i.ra.utils;

import java.io.InputStream;
import java.util.Objects;

public class ResourceUtil {
    public static <T> String getResourcePath(Class<T> relativeToClass, String resourceName) {
        return Objects.requireNonNull(relativeToClass.getClassLoader().getResource(resourceName)).getPath();
    }

    public static <T> InputStream getResourceStream(Class<T> relativeToClass, String resourceName) {
        return relativeToClass.getClassLoader().getResourceAsStream(resourceName);
    }

    public static <T> String getResourceAsString(Class<T> relativeToClass, String resourceName) {
        return FileIO.read(getResourcePath(relativeToClass, resourceName));
    }
}
