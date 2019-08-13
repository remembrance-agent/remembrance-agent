package io.p13i.ra.databases.cache.metadata;

import java.io.Serializable;

/**
 * Represents the metadata information about one file in the cache
 */
public class LocalDiskCacheDocumentMetadata implements Serializable {
    /**
     * The referenced file's metadata
     */
    public String fileName;

    /**
     * The subject of the file
     */
    public String subject;

    /**
     * The URL of the file
     */
    public String url;
}
