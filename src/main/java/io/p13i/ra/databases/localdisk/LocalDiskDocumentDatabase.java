package io.p13i.ra.databases.localdisk;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalDiskDocumentDatabase implements DocumentDatabase {

    private String directory;
    private List<Document> documents;

    public LocalDiskDocumentDatabase(String directory) {
        this.directory = directory;
    }

    @Override
    public String getName() {
        return LocalDiskDocumentDatabase.class.getName();
    }

    @Override
    public void loadDocuments() {
        documents = new ArrayList<>();
        List<String> documentsFilePaths = FileIO.listDirectory(this.directory);
        for (String documentFilePath : documentsFilePaths) {
            String fileName = FileIO.getFileName(documentFilePath);
            String fileContents = FileIO.read(documentFilePath);
            Date lastModified = FileIO.getLastModifiedDate(documentFilePath);
            this.documents.add(new LocalDiskDocument(fileContents, fileName, lastModified));
        }
    }

    @Override
    public void indexDocuments() {
        for (Document document : this.documents) {
            document.computeWordVector();
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return this.documents;
    }

    public static void main(String[] args) {
        LocalDiskDocumentDatabase documentDatabase = new LocalDiskDocumentDatabase("/Users/p13i/Projects/glass-notes/sample-documents");
        documentDatabase.loadDocuments();
        documentDatabase.indexDocuments();
        List<Document> documents = documentDatabase.getAllDocuments();
        for (Document document : documents) {
            System.out.println(document.toString());
        }
    }
}
