package io.p13i.ra.databases.cache;

import java.util.List;

interface LocalDiskCache {
    void loadDocumentsFromDiskIntoMemory();
    // Allow method chaining
    void saveDocumentsInMemoryToDisk();
    LocalDiskCacheDocumentDatabase addDocumentsToMemory(List<CachableDocument> cachableDocuments);
}

