package io.p13i.ra.databases.cache;

import java.util.List;

public interface LocalDiskCache {
    void loadDocumentsFromCache();
    void saveDocumentsToCache(List<CachableDocument> cachableDocuments);
}

