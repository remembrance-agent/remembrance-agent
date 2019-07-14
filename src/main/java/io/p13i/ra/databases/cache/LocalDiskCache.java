package io.p13i.ra.databases.cache;

import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public interface LocalDiskCache {
    void loadDocumentsFromCache();
    void saveDocumentsToCache(List<CachableDocument> cachableDocuments);
}

