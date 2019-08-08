package io.p13i.ra.databases.cache;

import java.util.List;

interface LocalDiskCache {
    void loadDocumentsFromCache();
    // Allow method chaining
    LocalDiskCacheDocumentDatabase saveDocumentsToCache(List<CachableDocument> cachableDocuments);
}

