package io.p13i.ra.databases.googledrive;

import io.p13i.ra.databases.cache.ICachableDocument;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.AbstractDocument;

import java.util.Date;
import java.util.Objects;

/**
 *
 */
class GoogleDriveDocument extends AbstractDocument implements ICachableDocument {
    public GoogleDriveDocument(String id, String content, String filename, Date lastModified) {
        super(content, new Context(null, null, filename, lastModified));
        this.url =  "https://docs.google.com/document/d/" + Objects.requireNonNull(id) + "/view";
    }
}
