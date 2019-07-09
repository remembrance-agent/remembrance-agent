package io.p13i.ra.databases;

import io.p13i.ra.models.Document;

import java.util.List;

public interface DocumentDatabase {
    void load();
    void index();
    List<Document> getAllDocuments();
}
