package io.p13i.ra.databases.localdisk;

import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.databases.cache.ICachableDocument;
import io.p13i.ra.databases.cache.ICachableDocumentDatabase;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the data store of files on local disk
 */
public class LocalDiskDocumentDatabase implements IDocumentDatabase<LocalDiskDocument>, ICachableDocumentDatabase {

    private static final Logger LOGGER = LoggerUtils.getLogger(LocalDiskDocumentDatabase.class);
    private final String directory;
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
        this.documents = new ArrayList<>();
        try {
            List<String> documentsFilePaths = FileIO.listFolderFilesRecursive(this.directory);
            LOGGER.info("Found " + documentsFilePaths.size() + " documents in " + this.directory);
            for (String documentFilePath : documentsFilePaths) {
                LOGGER.info("Examining file: " + documentFilePath);
                if (documentFilePath.endsWith(".txt") || documentFilePath.endsWith(".md")) {
                    String fileName = FileIO.getFileName(documentFilePath);
                    String fileContents = FileIO.read(documentFilePath);
                    Date lastModified = FileIO.getLastModifiedDate(documentFilePath);
                    this.documents.add(new LocalDiskDocument(fileContents, fileName, fileName, lastModified, documentFilePath));
                } else {
                    LOGGER.info("Skipping file because it doesn't end with .txt: " + documentFilePath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LocalDiskDocument> getAllDocuments() {
        return this.documents;
    }

    @Override
    public List<ICachableDocument> getDocumentsForSavingToCache() {
        return ListUtils.castUp(this.documents);
    }
}
