package io.p13i.ra.databases.cache;

import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;


public interface CachableDocument {

    default String getCacheHashCode() {
        return StringUtils.md5(FileIO.getCleanName(this.toString()));
    }

    String getContent();
}
