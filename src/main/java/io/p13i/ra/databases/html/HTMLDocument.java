package io.p13i.ra.databases.html;

import io.p13i.ra.databases.cache.ICachableDocument;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.models.Context;

import java.util.Date;

public class HTMLDocument extends AbstractDocument implements ICachableDocument {
    public HTMLDocument(String content, Date lastModified, String url, String subject) {
        super(content, new Context(null, null, subject, lastModified));
        this.setLastModified(lastModified);
        this.setURL(url);
    }
}
