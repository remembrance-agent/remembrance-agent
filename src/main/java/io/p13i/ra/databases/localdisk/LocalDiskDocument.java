package io.p13i.ra.databases.localdisk;

import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.DateUtils;

import java.util.Date;

/**
 * Represents one document on the local disk
 */
public class LocalDiskDocument extends Document {
    private Date lastModified;
    private String filename;

    public LocalDiskDocument(String content, String filename, Date lastModified) {
        super(content, new Context(null, null, filename, lastModified));
        this.filename = filename;
        this.lastModified = lastModified;
    }

    public String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "<" + LocalDiskDocument.class.getSimpleName() + " content='" + getContentTruncated() + "' filename='" + getFilename() + " last modified=" + DateUtils.timestampOf(this.lastModified) + "'>";
    }
}
