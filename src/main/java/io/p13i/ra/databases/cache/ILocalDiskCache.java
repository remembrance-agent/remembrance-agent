package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.AbstractDocumentDatabase;
import io.p13i.ra.models.AbstractDocument;

/**
 * Implementations of this interface allow data to be interchanged from the disk as a cache
 */
interface ILocalDiskCache {
    void loadDocumentsFromDiskIntoMemory();

    AbstractDocument getSingleDocumentFromDisk(String cacheFileName);

    void saveDocumentsInMemoryToDisk();

    String saveSingleDocumentToDisk(AbstractDocument document);

    void loadSingleDocumentFromDiskIntoMemory(String cachedFilePath);

    // Allow method chaining
    ILocalDiskCache addDocumentsToMemory(AbstractDocumentDatabase cachableDocumentDatabase);
}
