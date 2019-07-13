package io.p13i.ra.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListUtils {
    /**
     * Pretty print
     * @param list a list
     * @param <T> with a type
     * @return into a string
     */
    public static <T> String asString(List<T> list) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int i = 0;
        for (T item : list) {
            stringBuilder.append("'");
            stringBuilder.append(item.toString());
            stringBuilder.append("'");
            if (i < list.size() - 1) {
                stringBuilder.append(", ");
            }
            i++;
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }

    public static <T> List<T> fromArray(T[] array) {
        return fromArray(array, new Filter<T>() {/* use default implementation */});
    }

    /**
     * Gets a list
     * @param array from the given array
     * @param filter filtering certain elements
     * @param <T> of a given type
     * @return into a new ArrayList
     */
    public static <T> List<T> fromArray(T[] array, Filter<T> filter) {
        List<T> list = new ArrayList<T>(array.length);
        for (int i = 0; i < array.length; i++) {
            if (filter.shouldInclude(array[i])) {
                list.add(array[i]);
            }
        }
        return list;
    }

    interface Filter<T> {
        default boolean shouldInclude(T item) {
            return true;
        }
    }

    public static <T> List<T> intersection(List<T> first, List<T> second) {
        List<T> intersection = new LinkedList<>();
        for(T stringFromFirst : first) {
            if(second.contains(stringFromFirst)) {
                intersection.add(stringFromFirst);
            }
        }
        return intersection;
    }
}
