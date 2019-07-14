package io.p13i.ra.databases.googledrive;

import com.google.api.client.util.DateTime;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;

import java.util.Date;

public class GoogleDriveDocument extends Document {
    private String id;
    private Date lastModified;
    private String filename;

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
