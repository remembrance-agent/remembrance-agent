package io.p13i.ra.similarity;

import io.p13i.ra.utils.Assert;

/**
 * Represents a way to determine the similarity between two types
 * @param <T> the type in comparision
 */
interface ISimilarityIndex<T> {
    double INDEX_LOWER_BOUND = 0.0;
    double INDEX_HIGHER_BOUND = 1.0;
    double NO_SIMILARITY = 0.0;
    double calculate(T first, T second);

    default double checkInBounds(double value) {
        Assert.inRange(value, INDEX_LOWER_BOUND, INDEX_HIGHER_BOUND);
        return value;
    }
}
