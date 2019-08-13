package io.p13i.ra.databases.cache;

/**
 * Implementations of this interface allow data to be interchanged from the disk as a cache
 */
interface ILocalDiskCache {
    void loadDocumentsFromDiskIntoMemory();
    void saveDocumentsInMemoryToDisk();
    // Allow method chaining
    ILocalDiskCache addDocumentsToMemory(ICachableDocumentDatabase cachableDocumentDatabase);
}
