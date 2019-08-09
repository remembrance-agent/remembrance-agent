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
        return new LINQList<>(list).toString();
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
        return new ArrayList<>(source);
    }
}
