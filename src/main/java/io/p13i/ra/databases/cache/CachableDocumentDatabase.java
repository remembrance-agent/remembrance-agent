package io.p13i.ra.databases.cache;

import java.util.List;

public interface CachableDocumentDatabase {
    List<CachableDocument> getDocumentsForSavingToCache();
    List<CachableDocument> getDocumentsFromCache();
}
