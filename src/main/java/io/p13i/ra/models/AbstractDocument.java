package io.p13i.ra.models;

import io.p13i.ra.utils.FileIO;
import io.p13i.ra.utils.StringUtils;
import io.p13i.ra.utils.WordVector;

import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

/**
 * Houses all information about a file and it's context in the world
 */
public abstract class AbstractDocument implements Iterable<SingleContentWindow> {

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
    private MultipleContentWindows cachedContentWindows;

    /**
     * When this document was last modified
     */
    private Date lastModified;

    /**
     * The extension of cache files
     */
    public static final String CACHE_FILE_EXTENSION = ".cache.txt";

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

    public void index() {
        this.cachedContentWindows = WordVector.process(getContent());
    }

    public MultipleContentWindows getContentWindows() {
        if (cachedContentWindows == null) {
            throw new NullPointerException("indexing is required; did you call index()?");
        }
        return cachedContentWindows;
    }

    @Override
    public String toString() {
        return "<" + AbstractDocument.class.getSimpleName() + " content='" + getContentTruncated() + "'>";
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    /**
     * Gets the name of the document type
     *
     * @return the document type's name
     */
    public String getDocumentTypeName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Iterator<SingleContentWindow> iterator() {
        return this.getContentWindows().iterator();
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastModified() {
        return this.lastModified;
    }

    /**
     * @return the file name of a document in the cache
     */
    public String getCacheFileName() {
        return getCacheHashCode() + CACHE_FILE_EXTENSION;
    }

    /**
     * @return the hash code of the document
     */
    public String getCacheHashCode() {
        return StringUtils.md5(FileIO.getCleanName(this.toString()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getContent(), getContext(), getURL(), getLastModified());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        AbstractDocument other = (AbstractDocument) obj;
        return Objects.equals(this.getURL(), other.getURL())
                & Objects.equals(this.getContext(), other.getContext())
                & Objects.equals(this.getContent(), other.getContent())
                & Objects.equals(this.getLastModified(), other.getLastModified());
    }
}
