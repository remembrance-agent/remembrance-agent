package io.p13i.ra.models;

import java.util.List;

public interface DocumentDatabase {
    void load();
    void index();
    List<Document> getAllDocuments();
}
