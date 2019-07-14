package io.p13i.ra.similarity;

interface SimilarityIndex<T> {
    double INDEX_LOWER_BOUND = 0.0;
    double INDEX_HIGHER_BOUND = 1.0;
    double NO_SIMILARITY = 0.0;
    double calculate(T first, T second);
}
