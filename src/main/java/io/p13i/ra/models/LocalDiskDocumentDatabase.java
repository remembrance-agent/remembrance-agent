package io.p13i.ra.models;

import io.p13i.ra.utils.FileIO;

import java.util.ArrayList;
import java.util.List;

public class LocalDiskDocumentDatabase implements DocumentDatabase {

    private String directory;
    private List<Document> documents;

    public LocalDiskDocumentDatabase(String directory) {
        this.directory = directory;
    }

    @Override
    public void load() {
        documents = new ArrayList<>();
        List<String> documentsFilePaths = FileIO.listDirectory(this.directory);
        for (String documentFilePath : documentsFilePaths) {
            String fileContents = FileIO.read(documentFilePath);
            this.documents.add(new Document(fileContents));
        }
    }

    @Override
    public void index() {
        for (Document document : this.documents) {
            document.setContext(Context.of(document));
            document.computeWordVector();
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return this.documents;
    }

    public static void main(String[] args) {
        DocumentDatabase documentDatabase = new LocalDiskDocumentDatabase("/Users/p13i/Projects/glass-notes/sample-documents");
        documentDatabase.load();
        documentDatabase.index();
        List<Document> documents = documentDatabase.getAllDocuments();
        for (Document document : documents) {
            System.out.println(document.toString());
        }
    }
}
