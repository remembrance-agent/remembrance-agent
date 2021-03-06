package io.p13i.ra.cache;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


/**
 * Implementation of a cache
 */
public class Cache<TKey, TValue> extends AbstractCache<TKey, TValue> {

    /**
     * A list of caches that are used in this application.
     * Used so that all caches can be cleared at the same time.
     */
    private static List<Cache> managedCaches = new LinkedList<>();

    /**
     * Invalidates all caches managed by this application.
     */
    public static void invalidateManagedCaches() {
        for (Cache cache : managedCaches) {
            cache.invalidate();
        }
    }

    Map<TKey, TValue> mCache = new HashMap<>();

    public Cache() {
        this.invalidate();
        Cache.managedCaches.add(this);
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
        mCache = new HashMap<>();
    }
}
