package io.p13i.ra.utils;


import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class LINQList<T> {
    private final Iterable<T> source;
    private T default_ = null;

    public LINQList(Iterable<T> source) {
        this.source = source;
    }

    public static LINQList<Character> from(String string) {
        List<Character> charactersList = new ArrayList<>(string.length());
        for (int i = 0; i < string.length(); i++) {
            charactersList.add(string.charAt(i));
        }
        return new LINQList<>(charactersList);
    }

    public static LINQList<Integer> range(int start, int end) {
        Assert.that(start >= 0);
        Assert.that(end > 0);
        Assert.that(start < end);
        List<Integer> integerList = new ArrayList<>(end);
        for (int i = start; i < end; i++) {
            integerList.add(i);
        }
        return new LINQList<>(integerList);
    }


    public static LINQList<Integer> range(int end) {
        return range(0, end);
    }

    public LINQList<T> where(Function<T, Boolean> condition) {
        List<T> result = new LinkedList<>();
        for (T item : source) {
            if (condition.apply(item)) {
                result.add(item);
            }
        }
        return new LINQList<>(result);
    }

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

    public <TSelected> LINQList<TSelected> select(Function<T, TSelected> selector) {
        List<TSelected> result = new LinkedList<>();
        for (T item : source) {
            result.add(selector.apply(item));
        }
        return new LINQList<>(result);
    }

    public T default_() {
        return this.default_;
    }

    public LINQList<T> setDefault(T newDefault) {
        this.default_ = newDefault;
        return this;
    }

    public T first() throws NoSuchElementException {
        return this.source.iterator().next();
    }

    public T firstOrDefault() {
        try {
            return first();
        } catch (NoSuchElementException e) {
            return this.default_;
        }
    }

    public List<T> toList() {
        List<T> list = new LinkedList<>();
        for (T item : source) {
            list.add(item);
        }
        return list;
    }

    public LINQList<T> forEach(Consumer<T> consumer) {
        for (T item : source) {
            consumer.accept(item);
        }
        return this;
    }

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
