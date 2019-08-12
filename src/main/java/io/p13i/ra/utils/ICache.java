package io.p13i.ra.utils;

public interface ICache<TKey, TValue> {
    TValue get(TKey key);

    boolean hasKey(TKey key);

    void put(TKey key, TValue value);

    void invalidate();
}
