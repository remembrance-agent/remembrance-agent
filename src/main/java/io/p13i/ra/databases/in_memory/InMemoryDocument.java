package io.p13i.ra.databases.in_memory;

import io.p13i.ra.databases.cache.ICachableDocument;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.models.Context;

public class InMemoryDocument extends AbstractDocument implements ICachableDocument {
    public InMemoryDocument(String content, Context context) {
        super(content, context);
    }
}
