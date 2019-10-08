package io.p13i.ra.databases;

import io.p13i.ra.models.AbstractDocument;
import java.util.List;

public abstract class AbstractDocumentDatabase<TDocument extends AbstractDocument> {
    /**
     * Gets the name of the data store
     *
     * @return human readable name
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Loads all the documents from disk into memory
     */
    public abstract void loadDocuments();

    /**
     * Indexes all the documents
     */
    public void indexDocuments() {
        List<TDocument> documents = getAllDocuments();
        for (TDocument document : documents) {
            document.index();
        }
    }

    /**
     * Fetches the documents from memory
     *
     * @return All the documents
     */
    public abstract List<TDocument> getAllDocuments();
}
