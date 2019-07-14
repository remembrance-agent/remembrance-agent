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

    public LocalDiskDocument(String content, String documentFilePath, String filename, Date lastModified) {
        super(content, new Context(null, null, filename, lastModified));
        this.filename = filename;
        this.lastModified = lastModified;
        this.setURL(documentFilePath);
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
        String parentFolder = StringUtils.truncateWithEllipse(FileIO.getEnclosingFolderName(getUrl()), 10);
        String fileName = StringUtils.truncateWithEllipse(FileIO.getFileName(getUrl()), 9);
        return String.format("%s%s%s", parentFolder, File.separator, fileName);
    }
}
