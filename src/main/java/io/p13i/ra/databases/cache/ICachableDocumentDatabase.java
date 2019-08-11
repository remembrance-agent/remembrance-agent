package io.p13i.ra.databases.cache;

import java.util.List;

public interface ICachableDocumentDatabase {
    List<ICachableDocument> getDocumentsForSavingToCache();
}
