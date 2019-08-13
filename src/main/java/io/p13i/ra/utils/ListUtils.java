package io.p13i.ra.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;


/**
 * Utilities for dealing with Lists
 */
public class ListUtils {

    /**
     * Finds the common elements between two lists
     *
     * @param first  the first list
     * @param second the second list
     * @param <T>    the type of the elements
     * @return a list of the intersection
     */
    public static <T> List<T> intersection(List<T> first, List<T> second) {
        List<T> intersection = new LinkedList<>();
        for (T stringFromFirst : first) {
            if (second.contains(stringFromFirst)) {
                intersection.add(stringFromFirst);
            }
        }
        return intersection;
    }

    /**
     * Selects the largest elements from the given list
     *
     * @param list       the given list
     * @param maxCount   the maximum number of elements to get
     * @param comparator the comparator to use
     * @param <T>        the type of the elements
     * @return a list of maximum size {@code maxCount}
     */
    public static <T extends Comparable<T>> List<T> selectLargest(List<T> list, int maxCount, Comparator<T> comparator) {
        List<T> copy = new ArrayList<>(list);
        copy.sort(Collections.reverseOrder(comparator));
        return copy.subList(0, Math.min(maxCount, copy.size()));
    }

    /**
     * Casts the given list up
     *
     * @param source        the list
     * @param <TSubClass>   the sub class
     * @param <TSuperClass> the super class to cast to
     * @return the resultant casted list
     */
    public static <TSubClass extends TSuperClass, TSuperClass> List<TSuperClass> castUp(List<TSubClass> source) {
        return new ArrayList<>(source);
    }

    public static <T> String toString(List<T> list) {
        int count = list.size();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int i = 0;
        for (T item : list) {
            stringBuilder.append("'");
            stringBuilder.append(item.toString());
            stringBuilder.append("'");
            if (i < count - 1) {
                stringBuilder.append(", ");
            }
            i++;
        }
        stringBuilder.append(']');
        return stringBuilder.toString();
    }
}
