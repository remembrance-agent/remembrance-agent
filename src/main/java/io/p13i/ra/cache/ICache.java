package io.p13i.ra.cache;

import java.util.concurrent.Callable;

/**
 * Interface for a cache
 *
 * @param <TKey>   the cache key
 * @param <TValue> the value stored in the cache
 */
public abstract class ICache<TKey, TValue> {

    /**
     * Gets the key from the cache or null
     *
     * @param key the key
     * @return the value or null
     */
    public abstract TValue get(TKey key);

    /**
     * Gets the key from the cache if it exists or uses the defaultValueGenerator to insert and return a new value.
     * Useful for computationally-expensive calculations.
     *
     * @param key the key
     * @param defaultValueGenerator the default value generator only invoked if the key doesn't exist
     * @throws RuntimeException if the defaultValueGenerator produces an Exception
     * @return the keyed index or the result of the defaultValueGenerator
     */
    public TValue get(TKey key, Callable<TValue> defaultValueGenerator) {
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
    public abstract boolean hasKey(TKey key);

    /**
     * Puts a value in the cache
     *
     * @param key   the key
     * @param value the value
     */
    public abstract void put(TKey key, TValue value);

    /**
     * Refreshes the cache
     */
    public abstract void invalidate();
}
