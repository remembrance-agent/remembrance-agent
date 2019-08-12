package io.p13i.ra.utils;


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LimitedCapacityCache<TKey, TValue> implements ICache<TKey, TValue> {

    private int maxSize;
    private Map<TKey, TValue> mCache;


    public LimitedCapacityCache(int maxSize) {
        this.maxSize = maxSize;
        this.invalidate();
    }


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
        this.mCache = new LinkedHashMap<TKey, TValue>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<TKey, TValue> eldest) {
                return this.size() > maxSize;
            }
        };
    }
}
