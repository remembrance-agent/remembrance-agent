package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LocalDiskCacheDocumentDatabase implements DocumentDatabase {

    public static final String METADATA_FILENAME = "~metadata.json";

//    private final Metadata metadata;
    public String cacheLocalDirectory;
    public List<CachableDocument> cachableDocuments;
    public List<Document> documents;

    public LocalDiskCacheDocumentDatabase(String cacheLocalDirectory) {
        this.cacheLocalDirectory = cacheLocalDirectory;
//        this.metadata = JSONUtils.fromJson(FileIO.read(this.cacheLocalDirectory + File.separator + METADATA_FILENAME), Metadata.class);
    }

    @Override
    public String getName() {
        return LocalDiskCacheDocumentDatabase.class.getSimpleName();
    }

    @Override
    public void loadDocuments() {
        this.loadDocumentsFromCache();
    }

    public void loadDocumentsFromCache() {
        this.documents = new ArrayList<>();
        for (CachableDocument cachableDocument : this.cachableDocuments) {
            String content = FileIO.read(this.cacheLocalDirectory + File.separator + cachableDocument.getCacheUUID());
            this.documents.add(new Document(content));
        }
    }

    public void saveDocumentsToCache(List<CachableDocument> cachableDocuments) {
        for (CachableDocument cachableDocument : cachableDocuments) {
            FileIO.write(this.cacheLocalDirectory + File.separator + cachableDocument.getCacheUUID(), cachableDocument.getContent());
        }
    }

    @Override
    public List<Document> getAllDocuments() {
        return this.documents;
    }
}
