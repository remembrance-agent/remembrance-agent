package io.p13i.ra.databases.cache;

import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.cache.metadata.LocalDiskCacheDocumentMetadata;
import io.p13i.ra.databases.cache.metadata.LocalDiskCacheMetadata;
import io.p13i.ra.databases.cache.metadata.LocalDiskCacheMetadataParser;
import io.p13i.ra.databases.localdisk.LocalDiskDocument;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalDiskCacheDocumentDatabase implements DocumentDatabase, LocalDiskCache {

    private final String cacheLocalDirectory;
    private List<Document> documentsFromDisk = new ArrayList<>();
    private List<CachableDocument> documentsFromMemory = new ArrayList<>();

    private String getMetadataJSONFilePath() {
        return this.cacheLocalDirectory + File.separator + "~metadata.json";
    }

    public LocalDiskCacheDocumentDatabase(String cacheLocalDirectory) {
        this.cacheLocalDirectory = cacheLocalDirectory;
    }

    @Override
    public String getName() {
        return LocalDiskCacheDocumentDatabase.class.getSimpleName() +
                "(" + StringUtils.truncateBeginningWithEllipse(this.cacheLocalDirectory, 20)+ ")";
    }

    @Override
    public void loadDocuments() {
        this.loadDocumentsFromDiskIntoMemory();
    }

    @Override
    public void loadDocumentsFromDiskIntoMemory() {
        this.documentsFromDisk = new ArrayList<>();

        String metadataContents = FileIO.read(getMetadataJSONFilePath());
        LocalDiskCacheMetadata metadata = LocalDiskCacheMetadataParser.fromString(metadataContents);
        if (metadata == null) {
            return;
        }

        List<String> cachedFilePaths = FileIO.listFiles(this.cacheLocalDirectory);
        cachedFilePaths.remove(getMetadataJSONFilePath());
        for (String cachedFilePath : cachedFilePaths) {
            String fileName = FileIO.getFileName(cachedFilePath);
            String subject = metadata.fileNamesToMetadata.get(fileName).subject;
            String url = metadata.fileNamesToMetadata.get(fileName).url;
            String content = FileIO.read(cachedFilePath);
            Document document = new LocalDiskDocument(content, fileName, subject, FileIO.getLastModifiedDate(cachedFilePath), url);
            this.documentsFromDisk.add(document);
        }
    }

    @Override
    public void saveDocumentsInMemoryToDisk() {
        // Delete all documents already in cache
        List<String> documentsInCache = FileIO.listFiles(this.cacheLocalDirectory);
        for (String documentPath : documentsInCache) {
            FileIO.delete(documentPath);
        }

        // Save metadata file
        Map<String, LocalDiskCacheDocumentMetadata> fileNamesToMetadata = new HashMap<>();
        for (CachableDocument cachableDocument : this.documentsFromMemory) {
            fileNamesToMetadata.put(cachableDocument.getCacheFileName(), new LocalDiskCacheDocumentMetadata() {{
                fileName = cachableDocument.getCacheFileName();
                subject = cachableDocument.getContext().getSubject();
                url = cachableDocument.getURL();
            }});
        }
        FileIO.write(getMetadataJSONFilePath(), LocalDiskCacheMetadataParser.asString(new LocalDiskCacheMetadata(fileNamesToMetadata)));

        // Write each cache file
        for (CachableDocument cachableDocument : this.documentsFromMemory) {
            String cacheFileName = this.cacheLocalDirectory + File.separator + cachableDocument.getCacheFileName();
            FileIO.write(cacheFileName, cachableDocument.getContent());
        }
    }

    @Override
    public LocalDiskCacheDocumentDatabase addDocumentsToMemory(List<CachableDocument> cachableDocuments) {
        this.documentsFromMemory.addAll(cachableDocuments);
        return this;
    }

    @Override
    public List<Document> getAllDocuments() {
        return this.documentsFromDisk;
    }

}
