package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;
import java.util.Objects;

/**
 * Houses all information about a file and it's context in the world
 */
public abstract class AbstractDocument implements IRequiresIndexing {

    /**
     * How many characters to place in the toString() call
     */
    private static final int CONTENT_TRUNCATED_MAX_LENGTH = 20;

    /**
     * The raw content of the document
     */
    private final String content;

    /**
     * The context in which the document was created
     */
    private final Context context;

    /**
     * The URL of the document either on disk or the internet
     */
    protected String url;

    /**
     * The post-index property
     */
    private ContentWindow cachedContentWindow;

    public AbstractDocument(String content, Context context) {
        this.content = Objects.requireNonNull(content);
        this.context = Objects.requireNonNull(context);
    }

    public Context getContext() {
        return context;
    }

    public String getContent() {
        return content;
    }

    /**
     * Gets the content of the document truncated to CONTENT_TRUNCATED_MAX_LENGTH
     *
     * @return truncated contents
     */
    protected String getContentTruncated() {
        if (content == null) {
            return null;
        }
        boolean includeEllipses = this.content.length() > CONTENT_TRUNCATED_MAX_LENGTH;
        return content.substring(0, Math.min(this.content.length(), CONTENT_TRUNCATED_MAX_LENGTH)) + (includeEllipses ? "..." : "");
    }

    @Override
    public void index() {
        this.cachedContentWindow = WordVector.process(getContent());
    }

    public ContentWindow getContentWindow() {
        return cachedContentWindow;
    }

    @Override
    public String toString() {
        return "<" + AbstractDocument.class.getSimpleName() + " content='" + getContentTruncated() + "'>";
    }

    public String getURL() {
        return url;
    }

    /**
     * Gets the name of the document type
     *
     * @return the document type's name
     */
    public String getDocumentTypeName() {
        return this.getClass().getSimpleName();
    }
}
