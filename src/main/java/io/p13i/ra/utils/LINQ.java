package io.p13i.ra.utils;


import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class LINQ<T> {
    private final Iterable<T> iterable;
    private T default_ = null;

    public LINQ(Iterable<T> iterable) {
        this.iterable = iterable;
    }

    public LINQ<T> where(Function<T, Boolean> condition) {
        List<T> result = new LinkedList<>();
        for (T item : iterable) {
            if (condition.apply(item)) {
                result.add(item);
            }
        }
        return new LINQ<>(result);
    }

    public <TSelected> LINQ<TSelected> select(Function<T, TSelected> selector) {
        List<TSelected> result = new LinkedList<>();
        for (T item : iterable) {
            result.add(selector.apply(item));
        }
        return new LINQ<>(result);
    }

    public T default_() {
        return this.default_;
    }

    public LINQ<T> setDefault(T newDefault) {
        this.default_ = newDefault;
        return this;
    }

    public T first() throws NoSuchElementException {
        return this.iterable.iterator().next();
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
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }

    public int count() {
        int n = 0;
        for (T ignored : this.iterable) {
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
        for (T item : iterable) {
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
