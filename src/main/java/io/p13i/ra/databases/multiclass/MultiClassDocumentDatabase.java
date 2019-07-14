package io.p13i.ra.databases.multiclass;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.models.Document;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MultiClassDocumentDatabase implements DocumentDatabase {

    private List<DocumentDatabase> documentDatabases;

    public MultiClassDocumentDatabase() {
        this.documentDatabases = new LinkedList<>();
    }

    public MultiClassDocumentDatabase addDocumentDatabase(DocumentDatabase documentDatabase) {
        this.documentDatabases.add(documentDatabase);
        return this;
    }

    @Override
    public String getName() {
        return MultiClassDocumentDatabase.class.getSimpleName();
    }

    @Override
    public void loadDocuments() {
        for (DocumentDatabase db : this.documentDatabases) {
            db.loadDocuments();
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        List<Document> documents = new ArrayList<>();
        for (DocumentDatabase db : this.documentDatabases) {
            documents.addAll(db.getAllDocuments());
        }
        return documents;
    }
}
