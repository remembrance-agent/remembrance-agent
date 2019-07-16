package io.p13i.ra.databases.cache;

import io.p13i.ra.models.Context;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;


public interface CachableDocument {

    default String getCacheFileName() {
        return getCacheHashCode() + ".txt";
    }

    default String getCacheHashCode() {
        return StringUtils.md5(FileIO.getCleanName(this.toString()));
    }

    String getContent();

    Context getContext();
}
