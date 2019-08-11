package io.p13i.ra.databases;

import io.p13i.ra.models.AbstractDocument;

import java.util.List;

public interface IDocumentDatabase<TDocument extends AbstractDocument> {
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
    default void indexDocuments() {
        List<TDocument> documents = getAllDocuments();
        for (TDocument document : documents) {
            document.computeWordVector();
        }
    }

    /**
     * Fetches the documents from memory
     * @return All the documents
     */
    List<TDocument> getAllDocuments();
}
