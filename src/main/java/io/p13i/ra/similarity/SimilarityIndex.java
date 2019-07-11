package io.p13i.ra.similarity;

public interface SimilarityIndex<T> {
    public static final double NO_SIMILARITY = 0.0;
    double calculate(T first, T second);
}
