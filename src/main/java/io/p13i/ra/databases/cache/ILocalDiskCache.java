package io.p13i.ra.databases.cache;

interface ILocalDiskCache {
    void loadDocumentsFromDiskIntoMemory();
    void saveDocumentsInMemoryToDisk();
    // Allow method chaining
    LocalDiskCacheDocumentDatabase addDocumentsToMemory(ICachableDocumentDatabase cachableDocumentDatabase);
}

