package io.p13i.ra.cache;


import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A cache limited in size
 */
public class LimitedCapacityCache<TKey, TValue> extends Cache<TKey, TValue> {

    private int maxSize;

    public LimitedCapacityCache(int maxSize) {
        super();
        this.maxSize = maxSize;
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
