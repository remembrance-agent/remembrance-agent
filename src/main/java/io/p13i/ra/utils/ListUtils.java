package io.p13i.ra.utils;

import java.util.*;

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
    private static <T> List<T> fromArray(T[] array, Filter<T> filter) {
        List<T> list = new ArrayList<>(array.length);
        for (T t : array) {
            if (filter.shouldInclude(t)) {
                list.add(t);
            }
        }
        return list;
    }

    public static <T> T[] asArray(List<T> list) {
        T[] array = (T[]) new Object[list.size()];

        int i = 0;
        for (T item : list) {
            array[i++] = item;
        }

        return array;
    }

    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        List<T> filteredList = new ArrayList<>(list.size());
        for (T item : list) {
            if (filter.shouldInclude(item)) {
                filteredList.add(item);
            }
        }
        return filteredList;
    }

    public interface Filter<T> {
        @SuppressWarnings("SameReturnValue")
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

    private static <T> List<T> copy(List<T> source) {
        return new ArrayList<>(source);
    }

    public static <T extends Comparable<T>> List<T> selectLargest(List<T> list, int maxCount, Comparator<T> comparator) {
        List<T> copy = copy(list);
        copy.sort(Collections.reverseOrder(comparator));
        return copy.subList(0, Math.min(maxCount, copy.size()));
    }

    public static <TSubClass extends TSuperClass, TSuperClass> List<TSuperClass> castUp(List<TSubClass> source) {
        List<TSuperClass> newList = new ArrayList<>(source);
        return newList;
    }
}
