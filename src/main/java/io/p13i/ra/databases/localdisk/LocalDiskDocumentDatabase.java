package io.p13i.ra.databases.localdisk;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.databases.cache.CachableDocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.LoggerUtils;
import jdk.vm.ci.meta.Local;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the data store of files on local disk
 */
public class LocalDiskDocumentDatabase implements DocumentDatabase, CachableDocumentDatabase {

    private static final Logger LOGGER = LoggerUtils.getLogger(LocalDiskDocumentDatabase.class);
    private String directory;
    private List<LocalDiskDocument> documents;

    /**
     * @param directory The directory to index
     */
    public LocalDiskDocumentDatabase(String directory) {
        this.directory = directory;
    }

    @Override
    public String getName() {
        return LocalDiskDocumentDatabase.class.getName();
    }

    @Override
    public void loadDocuments() {
        this.documents = new ArrayList<LocalDiskDocument>();
        try {
            List<String> documentsFilePaths = FileIO.listFolderFilesRecursive(this.directory);
            LOGGER.info("Found " + documentsFilePaths.size() + " documents in " + this.directory);
            for (String documentFilePath : documentsFilePaths) {
                LOGGER.info("Examining file: " + documentFilePath);
                if (documentFilePath.endsWith(".txt") || documentFilePath.endsWith(".md")) {
                    String fileName = FileIO.getFileName(documentFilePath);
                    String fileContents = FileIO.read(documentFilePath);
                    Date lastModified = FileIO.getLastModifiedDate(documentFilePath);
                    this.documents.add(new LocalDiskDocument(fileContents, documentFilePath, fileName, lastModified));
                } else {
                    LOGGER.info("Skipping file because it doesn't end with .txt: " + documentFilePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return ListUtils.castUp(this.documents, Document.class);
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

    @Override
    public List<CachableDocument> getDocumentsForSavingToCache() {
        return ListUtils.castUp(this.documents, CachableDocument.class);
    }
}
