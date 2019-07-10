package io.p13i.ra.databases.localdisk;

import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;

import java.util.Date;

public class LocalDiskDocument extends Document {
    private Date lastModified;
    private String filename;

    public LocalDiskDocument(String content, String filename, Date lastModified) {
        super(content);
        this.filename = filename;
        this.lastModified = lastModified;
        this.context = new Context(null, null, this.filename, this.lastModified);
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "<" + LocalDiskDocument.class.getSimpleName() + " content='" + getContentTruncated() + "' filename='" + getFilename() + "'>";
    }
}
