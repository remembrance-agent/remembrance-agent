package io.p13i.ra.databases.gmail;

import io.p13i.ra.databases.cache.CachableDocument;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;

import java.util.Date;

public class GmailDocument extends Document implements CachableDocument {

    public GmailDocument(String body, String subject, String sender, Date date) {
        super(body, new Context(null, sender, subject, date));
    }

}
