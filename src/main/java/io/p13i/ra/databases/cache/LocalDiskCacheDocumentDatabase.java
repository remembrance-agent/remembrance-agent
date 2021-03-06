package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.AbstractDocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocument;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.utils.DateUtils;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.LoggerUtils;
import io.p13i.ra.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class LocalDiskCacheDocumentDatabase extends AbstractDocumentDatabase<AbstractDocument> implements ILocalDiskCache {

    private static final Logger LOGGER = LoggerUtils.getLogger(LocalDiskCacheDocumentDatabase.class);

    private final String cacheLocalDirectory;
    private List<AbstractDocument> documentsFromDisk = new ArrayList<>();
    private List<AbstractDocument> documentsFromMemory = new ArrayList<>();

    public LocalDiskCacheDocumentDatabase(String cacheLocalDirectory) {
        this.cacheLocalDirectory = cacheLocalDirectory;
    }

    @Override
    public String getName() {
        return LocalDiskCacheDocumentDatabase.class.getSimpleName() +
                "(" + StringUtils.truncateBeginningWithEllipse(this.cacheLocalDirectory, 20) + ")";
    }

    @Override
    public void loadDocuments() {
        this.loadDocumentsFromDiskIntoMemory();
    }

    @Override
    public void loadDocumentsFromDiskIntoMemory() {
        LOGGER.info("LocalDiskCacheDocumentDatabase::loadDocumentsFromDiskIntoMemory()");

        this.documentsFromDisk = new ArrayList<>();

        if (!FileIO.directoryExists(this.cacheLocalDirectory)) {
            return;
        }

        List<String> cachedFilePaths = FileIO.listFiles(this.cacheLocalDirectory);
        for (String filePath : cachedFilePaths) {
            if (!filePath.endsWith(AbstractDocument.CACHE_FILE_EXTENSION)) {
                continue;
            }

            this.documentsFromDisk.add(getSingleDocumentFromDisk(filePath));
        }
    }

    @Override
    public void loadSingleDocumentFromDiskIntoMemory(String cachedFilePath) {
        LOGGER.info("LocalDiskCacheDocumentDatabase::loadSingleDocumentFromDiskIntoMemory");
        this.documentsFromDisk.add(getSingleDocumentFromDisk(cachedFilePath));
    }

    @Override
    public AbstractDocument getSingleDocumentFromDisk(String cachedFilePath) {
        LOGGER.info("LocalDiskCacheDocumentDatabase::getSingleDocumentFromDisk");

        String fileContents = FileIO.read(cachedFilePath);
        String[] lines = StringUtils.lines(fileContents);

        String fileName = FileIO.getFileName(cachedFilePath);
        String version = lines[1].substring(16);
        String lastModified = lines[2].substring(16);
        String subject = lines[3].substring(16);
        String url = lines[4].substring(16);

        String content;
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 6; i < lines.length; i++) {
                stringBuilder.append(lines[i]);
            }
            content = stringBuilder.toString();
        }

        // Checks and conversions
        if (!version.equals(RemembranceAgentEngine.VERSION)) {
            return null;
        }
        Date lastModifiedDate = DateUtils.parseTimestamp(lastModified);

        return new LocalDiskDocument(content, fileName, subject, lastModifiedDate, url) {{
            index();
        }};
    }

    @Override
    public void saveDocumentsInMemoryToDisk() {
        // Write each cache file
        for (AbstractDocument cachableDocument : this.documentsFromMemory) {
            this.saveSingleDocumentToDisk(cachableDocument);
        }
    }

    @Override
    public String saveSingleDocumentToDisk(AbstractDocument cachableDocument) {
        LOGGER.info("LocalDiskCacheDocumentDatabase::saveSingleDocumentToDisk");

        String cacheFileName = this.cacheLocalDirectory + File.separator + cachableDocument.getCacheFileName();

        FileIO.delete(cacheFileName);

        FileIO.append(cacheFileName, "---");
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "Version         " + RemembranceAgentEngine.VERSION);
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "Last modified   " + DateUtils.timestampOf(cachableDocument.getLastModified()));
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "Subject         " + cachableDocument.getContext().getSubject());
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "URL             " + cachableDocument.getURL());
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "---");
        FileIO.newline(cacheFileName);

        FileIO.append(cacheFileName, cachableDocument.getContent());

        return cacheFileName;
    }

    @Override
    public LocalDiskCacheDocumentDatabase addDocumentsToMemory(AbstractDocumentDatabase cachableDocumentDatabase) {
        this.documentsFromMemory.addAll(cachableDocumentDatabase.getAllDocuments());
        return this;
    }

    @Override
    public List<AbstractDocument> getAllDocuments() {
        return this.documentsFromDisk;
    }

}
