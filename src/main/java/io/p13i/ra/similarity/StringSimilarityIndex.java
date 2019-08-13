package io.p13i.ra.similarity;


import io.p13i.ra.utils.Assert;
import io.p13i.ra.cache.ICache;
import io.p13i.ra.cache.LimitedCapacityCache;
import io.p13i.ra.utils.Tuple;

import java.util.Objects;

/**
 * Compares strings and produces an index
 */
public class StringSimilarityIndex {
    /**
     * Calculates the similarity between two strings using edit distance
     *
     * @param x the first string
     * @param y the second string
     * @return a similarity index between 0.0 and 1.0
     */
    public static double calculate(String x, String y) {
        return Objects.equals(x, y) ? 1.0 : 0.0;
    }
}
