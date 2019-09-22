package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.databases.cache.metadata.LocalDiskCacheDocumentMetadata;
import io.p13i.ra.databases.cache.metadata.LocalDiskCacheMetadata;
import io.p13i.ra.databases.localdisk.LocalDiskDocument;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class LocalDiskCacheDocumentDatabase implements IDocumentDatabase<AbstractDocument>, ILocalDiskCache {

    private final String cacheLocalDirectory;
    private List<AbstractDocument> documentsFromDisk = new ArrayList<>();
    private List<ICachableDocument> documentsFromMemory = new ArrayList<>();

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
        this.documentsFromDisk = new ArrayList<>();

        if (!FileIO.directoryExists(this.cacheLocalDirectory)) {
            return;
        }

        List<String> cachedFilePaths = FileIO.listFiles(this.cacheLocalDirectory);
        for (String cachedFilePath : cachedFilePaths) {
            this.documentsFromDisk.add(getSingleDocumentFromDisk(cachedFilePath));
        }
    }

    @Override
    public void loadSingleDocumentFromDiskIntoMemory(String cachedFilePath) {
        this.documentsFromDisk.add(getSingleDocumentFromDisk(cachedFilePath));
    }

    @Override
    public AbstractDocument getSingleDocumentFromDisk(String cachedFilePath) {
        String fileContents = FileIO.read(cachedFilePath);
        String[] lines = StringUtils.lines(fileContents);

        String fileName = FileIO.getFileName(cachedFilePath);
        String subject = lines[1].substring(12);
        String url = lines[2].substring(12);
        String content = Arrays.stream(lines)
                .skip(4)
                .collect(Collectors.joining("\n"));

        AbstractDocument document = new LocalDiskDocument(content, fileName, subject, FileIO.getLastModifiedDate(cachedFilePath), url);

        return document;
    }

    @Override
    public void saveDocumentsInMemoryToDisk() {
        // Write each cache file
        for (ICachableDocument cachableDocument : this.documentsFromMemory) {
            this.saveSingleDocumentToDisk(cachableDocument);
        }
    }

    @Override
    public String saveSingleDocumentToDisk(ICachableDocument cachableDocument) {
        String cacheFileName = this.cacheLocalDirectory + File.separator + cachableDocument.getCacheFileName();

        FileIO.delete(cacheFileName);

        FileIO.append(cacheFileName, "---");
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "Subject     " + cachableDocument.getContext().getSubject());
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "URL         " + cachableDocument.getURL());
        FileIO.newline(cacheFileName);
        FileIO.append(cacheFileName, "---");
        FileIO.newline(cacheFileName);

        FileIO.append(cacheFileName, cachableDocument.getContent());

        return cacheFileName;
    }

    @Override
    public LocalDiskCacheDocumentDatabase addDocumentsToMemory(ICachableDocumentDatabase cachableDocumentDatabase) {
        this.documentsFromMemory.addAll(cachableDocumentDatabase.getAllDocuments());
        return this;
    }

    @Override
    public List<AbstractDocument> getAllDocuments() {
        return this.documentsFromDisk;
    }

}
