package io.p13i.ra.databases;

import io.p13i.ra.models.Document;

import java.util.List;

public interface DocumentDatabase {
    void loadDocuments();
    void indexDocuments();
    List<Document> getAllDocuments();
}
