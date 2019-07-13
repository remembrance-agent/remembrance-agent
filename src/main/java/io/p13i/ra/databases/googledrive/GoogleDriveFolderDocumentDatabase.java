package io.p13i.ra.databases.googledrive;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocument;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.models.Document;

import java.util.List;

public class GoogleDriveFolderDocumentDatabase implements DocumentDatabase {

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void loadDocuments() {

    }

    @Override
    public void indexDocuments() {

    }

    @Override
    public List<Document> getAllDocuments() {
        return null;
    }

    public LocalDiskDocumentDatabase toLocalDiskDocumentDatabase() {
        return new LocalDiskDocumentDatabase("tmp directory");
    }
}
