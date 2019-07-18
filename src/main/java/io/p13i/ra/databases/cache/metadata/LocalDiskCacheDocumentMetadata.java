package io.p13i.ra.databases.cache.metadata;

import java.io.Serializable;

/**
 * Represents the metadata information about one file in the cache
 */
public class LocalDiskCacheDocumentMetadata implements Serializable {
    public String fileName;
    public String subject;
    public String url;
}
