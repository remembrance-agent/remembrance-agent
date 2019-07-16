package io.p13i.ra.databases.cache.metadata;

import java.io.Serializable;
import java.util.Map;

public class LocalDiskCacheMetadata implements Serializable {
    public Map<String, LocalDiskCacheDocumentMetadata> fileNamesToMetadata;

    public LocalDiskCacheMetadata(Map<String, LocalDiskCacheDocumentMetadata> fileNamesToMetadata) {
        this.fileNamesToMetadata = fileNamesToMetadata;
    }
}
