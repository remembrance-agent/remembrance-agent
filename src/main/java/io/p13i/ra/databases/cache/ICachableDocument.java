package io.p13i.ra.databases.cache;

import io.p13i.ra.models.Context;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;


/**
 * Represents one object that can be cached
 */
public interface ICachableDocument {

    /**
     * The extension of cache files
     */
    String CACHE_FILE_EXTENSION = ".cache.txt";

    /**
     * @return the file name of a document in the cache
     */
    default String getCacheFileName() {
        return getCacheHashCode() + CACHE_FILE_EXTENSION;
    }

    /**
     * @return the hash code of the document
     */
    default String getCacheHashCode() {
        return StringUtils.md5(FileIO.getCleanName(this.toString()));
    }

    /**
     * @return the content of the document
     */
    String getContent();

    /**
     * @return the context of the document
     */
    Context getContext();

    /**
     * @return the URL of the document
     */
    String getURL();
}
