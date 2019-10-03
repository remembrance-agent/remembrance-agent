package io.p13i.ra.databases.in_memory;

import io.p13i.ra.databases.IDocumentDatabase;

import java.util.List;

public class InMemoryDocumentDatabase implements IDocumentDatabase<InMemoryDocument> {
    private final List<InMemoryDocument> mDocuments;

    public InMemoryDocumentDatabase(List<InMemoryDocument> documents) {
        mDocuments = documents;
    }

    @Override
    public void loadDocuments() {

    }

    @Override
    public List<InMemoryDocument> getAllDocuments() {
        return mDocuments;
    }
}
