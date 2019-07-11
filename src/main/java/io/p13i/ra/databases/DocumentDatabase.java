package io.p13i.ra.databases;

import io.p13i.ra.models.Document;

import java.util.List;

public interface DocumentDatabase {
    /**
     * Gets the name of the data store
     * @return human readable name
     */
    String getName();

    /**
     * Loads all the documents from disk into memory
     */
    void loadDocuments();

    /**
     * Indexes all the documents
     */
    void indexDocuments();

    /**
     * Fetches the documents from memory
     * @return All the documents
     */
    List<Document> getAllDocuments();
}
