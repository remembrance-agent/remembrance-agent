package io.p13i.ra.models;

/**
 * Represents models that use cached properties and indexing to run efficiently
 */
public interface IRequiresIndexing {
    /**
     * Runs a long-running or computationally intensive process.
     */
    void index();
}
