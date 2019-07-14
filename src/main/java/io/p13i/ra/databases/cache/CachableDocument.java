package io.p13i.ra.databases.cache;

import io.p13i.ra.models.Document;

public interface CachableDocument {
    String getCacheUUID();
    String getContent();
}
