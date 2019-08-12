package io.p13i.ra.utils;

import java.util.HashMap;
import java.util.Map;

public class Cache<TKey, TValue> implements ICache<TKey, TValue> {
    private Map<TKey, TValue> mCache = new HashMap<>();

    @Override
    public TValue get(TKey key) {
        if (!hasKey(key)) {
            return null;
        }
        return mCache.get(key);
    }

    @Override
    public boolean hasKey(TKey key) {
        return mCache.containsKey(key);
    }

    @Override
    public void put(TKey key, TValue value) {
        mCache.put(key, value);
    }

    @Override
    public void invalidate() {
        mCache = new HashMap<>();
    }
}
