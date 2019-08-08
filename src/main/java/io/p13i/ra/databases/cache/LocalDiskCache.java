package io.p13i.ra.databases.cache;

import java.util.List;

interface LocalDiskCache {
    void loadDocumentsFromDiskIntoMemory();
    void saveDocumentsInMemoryToDisk();
    // Allow method chaining
    LocalDiskCacheDocumentDatabase addDocumentsToMemory(CachableDocumentDatabase cachableDocumentDatabase);
}

