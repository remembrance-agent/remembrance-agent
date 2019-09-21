package io.p13i.ra.cache;

import java.util.concurrent.Callable;

/**
 * Interface for a cache
 *
 * @param <TKey>   the cache key
 * @param <TValue> the value stored in the cache
 */
public interface ICache<TKey, TValue> {

    /**
     * Gets the key from the cache or null
     *
     * @param key the key
     * @return the value or null
     */
    TValue get(TKey key);

    default TValue get(TKey key, Callable<TValue> defaultValueGenerator) {
        TValue value;
        if (hasKey(key)) {
            value = get(key);
        } else {
            try {
                value = defaultValueGenerator.call();
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            put(key, value);
        }

        return value;
    }

    /**
     * Checks if key exists in cache
     *
     * @param key the key
     * @return whether or not the element exists in the cache
     */
    boolean hasKey(TKey key);

    /**
     * Puts a value in the cache
     *
     * @param key   the key
     * @param value the value
     */
    void put(TKey key, TValue value);

    /**
     * Refreshes the cache
     */
    void invalidate();
}
