package io.p13i.ra.databases.localdisk;

import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.LoggerUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents the data store of files on local disk
 */
public class LocalDiskDocumentDatabase extends IDocumentDatabase<LocalDiskDocument> {

    private static final Logger LOGGER = LoggerUtils.getLogger(LocalDiskDocumentDatabase.class);
    private final String directory;
    private List<LocalDiskDocument> documents;

    /**
     * @param directory The local directory to index
     */
    public LocalDiskDocumentDatabase(String directory) {
        this.directory = directory;
    }

    @Override
    public void loadDocuments() {
        this.documents = new ArrayList<>();
        try {
            List<String> documentsFilePaths = FileIO.listFolderFilesRecursive(this.directory);
            for (String documentFilePath : documentsFilePaths) {
                if (documentFilePath.endsWith(".txt") || documentFilePath.endsWith(".md")) {
                    String fileName = FileIO.getFileName(documentFilePath);
                    String fileContents = FileIO.read(documentFilePath);
                    Date lastModified = FileIO.getLastModifiedDate(documentFilePath);
                    this.documents.add(new LocalDiskDocument(fileContents, fileName, fileName, lastModified, documentFilePath));
                } else {
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
}
