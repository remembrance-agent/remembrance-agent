package io.p13i.ra.databases.cache;

import io.p13i.ra.models.Context;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;


/**
 * Represents one object that can be cached
 */
public interface CachableDocument {

    String CACHE_FILE_EXTENSION = "cache.txt";

    default String getCacheFileName() {
        return getCacheHashCode() + "." + CACHE_FILE_EXTENSION;
    }

    default String getCacheHashCode() {
        return StringUtils.md5(FileIO.getCleanName(this.toString()));
    }

    String getContent();

    Context getContext();

    String getURL();
}
