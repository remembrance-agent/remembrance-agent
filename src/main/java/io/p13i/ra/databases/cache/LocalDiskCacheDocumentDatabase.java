package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocument;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalDiskCacheDocumentDatabase implements DocumentDatabase, LocalDiskCache {

    public String cacheLocalDirectory;
    public List<CachableDocument> cachableDocuments;
    public List<Document> documents;

    public LocalDiskCacheDocumentDatabase(String cacheLocalDirectory) {
        this.cacheLocalDirectory = cacheLocalDirectory;
    }

    @Override
    public String getName() {
        return LocalDiskCacheDocumentDatabase.class.getSimpleName();
    }

    @Override
    public void loadDocuments() {
        this.loadDocumentsFromCache();
    }

    @Override
    public void loadDocumentsFromCache() {
        this.documents = new ArrayList<>();
        for (String cachedFilePath : FileIO.listFiles(this.cacheLocalDirectory)) {
            String content = FileIO.read(cachedFilePath);
            Document document = new LocalDiskDocument(content, cachedFilePath, FileIO.getFileName(cachedFilePath), FileIO.getLastModifiedDate(cachedFilePath));
            this.documents.add(document);
        }
    }

    public void saveDocumentsToCache(List<CachableDocument> cachableDocuments) {
        this.cachableDocuments = cachableDocuments;
        for (CachableDocument cachableDocument : cachableDocuments) {
            FileIO.write(this.cacheLocalDirectory + File.separator + cachableDocument.getCacheHashCode() + ".txt", cachableDocument.getContent());
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return this.documents;
    }
}
