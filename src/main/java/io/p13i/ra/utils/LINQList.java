package io.p13i.ra.utils;


import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An inefficient implementation of some of the IEnumerable extensions from C#
 * @param <T> the type of the elements
 */
public class LINQList<T> {
    private final Iterable<T> source;
    private T default_ = null;

    public LINQList(Iterable<T> source) {
        this.source = source;
    }

    /**
     * Generates a {@code LINQList} from a string
     * @param string the string
     * @return a {@code LINQList}
     */
    public static LINQList<Character> from(String string) {
        List<Character> charactersList = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            charactersList.add(string.charAt(i));
        }
        return new LINQList<>(charactersList);
    }

    /**
     * Generates a {@code LINQList} from an iterable
     * @param source the iterable
     * @param <T> the type of the elements
     * @return a {@code LINQList}
     */
    public static <T> LINQList<T> from(Iterable<T> source) {
        return new LINQList<>(source);
    }

    /**
     * Generates a {@code LINQList} from an array
     * @param source the iterable
     * @param <T> the type of the elements
     * @return a {@code LINQList}
     */
    public static <T> LINQList<T> from(T[] source) {
        return new LINQList<>(Arrays.asList(source));
    }

    /**
     * Generates a {@code LINQList} for elements matching a condition
     * @param condition the condition
     * @return a new {@code LINQList}
     */
    public LINQList<T> where(Function<T, Boolean> condition) {
        List<T> result = new LinkedList<>();
        for (T item : source) {
            if (condition.apply(item)) {
                result.add(item);
            }
        }
        return new LINQList<>(result);
    }

    /**
     * Gets the first {@code count} elements from a {@code LINQList}
     * @param count the first elements to take
     * @return a new {@code LINQList}
     */
    public LINQList<T> take(int count) {
        List<T> result = new LinkedList<>();
        int n = 0;
        for (T t : this.source) {
            if (++n > count) {
                break;
            }
            result.add(t);
        }
        return new LINQList<>(result);
    }

    /**
     * Maps a {@code LINQList} to new elements
     * @param selector the mapper
     * @param <TSelected> the selected type
     * @return a new {@code LINQList} of the selected type
     */
    public <TSelected> LINQList<TSelected> select(Function<T, TSelected> selector) {
        List<TSelected> result = new LinkedList<>();
        for (T item : source) {
            result.add(selector.apply(item));
        }
        return new LINQList<>(result);
    }

    /**
     * Gets the first element of the iterable
     * @return the first element
     * @throws NoSuchElementException or throws an exception if there is no first element
     */
    public T first() throws NoSuchElementException {
        return this.source.iterator().next();
    }

    /**
     * Gets the first element or null
     * @return first element or null
     */
    public T firstOrDefault() {
        try {
            return first();
        } catch (NoSuchElementException e) {
            return this.default_;
        }
    }

    /**
     * Converts the iterable to a list
     * @return a new list
     */
    public List<T> toList() {
        List<T> list = new LinkedList<>();
        for (T item : source) {
            list.add(item);
        }
        return list;
    }

    /**
     * Runs code for each element in the list
     * @param consumer the code to run
     * @return the same {@code LINQList}
     */
    public LINQList<T> forEach(Consumer<T> consumer) {
        for (T item : source) {
            consumer.accept(item);
        }
        return this;
    }

    /**
     * Counts the elements in the {@code LINQList}
     * @return a count
     */
    public int count() {
        int n = 0;
        for (T ignored : this.source) {
            n++;
        }
        return n;
    }

    @Override
    public String toString() {
        int count = this.count();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int i = 0;
        for (T item : source) {
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
