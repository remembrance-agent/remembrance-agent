package io.p13i.ra.databases.localdisk;

import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.DateUtils;
import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;

import java.io.File;
import java.util.Date;

/**
 * Represents one document on the local disk
 */
public class LocalDiskDocument extends Document implements CachableDocument {
    private final Date lastModified;
    private final String filename;

    public LocalDiskDocument(String content, String filename, String subject, Date lastModified, String url) {
        super(content, new Context(null, null, subject, lastModified));
        this.filename = filename;
        this.lastModified = lastModified;
        this.url = url;
    }

    private String getFilename() {
        return filename;
    }

    @Override
    public String toString() {
        return "<" + LocalDiskDocument.class.getSimpleName() + " content='" + getContentTruncated() + "' filename='" + getFilename() + " last modified=" + DateUtils.timestampOf(this.lastModified) + "'>";
    }

    @Override
    public String toTruncatedUrlString() {
        String parentFolder = StringUtils.truncateEndWithEllipse(FileIO.getEnclosingFolderName(this.getURL()), 10);
        String fileName = StringUtils.truncateEndWithEllipse(FileIO.getFileName(this.getURL()), 9);
        return String.format("%s%s%s", parentFolder, File.separator, fileName);
    }

    @Override
    public String getDocumentTypeName() {
        return LocalDiskDocument.class.getSimpleName();
    }
}
