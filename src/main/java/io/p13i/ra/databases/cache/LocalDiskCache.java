package io.p13i.ra.databases.cache;

import java.util.List;

interface LocalDiskCache {
    void loadDocumentsFromCache();
    void saveDocumentsToCache(List<CachableDocument> cachableDocuments);
}

