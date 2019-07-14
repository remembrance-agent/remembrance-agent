package io.p13i.ra.databases.googledrive;

import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;

import java.util.Date;

class GoogleDriveDocument extends Document implements CachableDocument {
    private final String id;
    private final Date lastModified;
    private final String filename;

    public GoogleDriveDocument(String id, String content, String filename, Date lastModified) {
        super(content, new Context(null, null, filename, lastModified));
        this.id = id;
        this.filename = filename;
        this.lastModified = lastModified;
        this.setURL("https://docs.google.com/document/d/" + id + "/view");
    }

    public String getId() {
        return id;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getFilename() {
        return filename;
    }
}
