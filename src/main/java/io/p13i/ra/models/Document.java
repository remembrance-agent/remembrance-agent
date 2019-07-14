package io.p13i.ra.models;

import io.p13i.ra.utils.StringUtils;
import io.p13i.ra.utils.WordVector;

import java.util.List;

/**
 * Houses all information about a file and it's context in the world
 */
public class Document {
    /**
     * How many characters to place in the toString() call
     */
    private static final int CONTENT_TRUNCATED_MAX_LENGTH = 20;

    private final String content;
    private final Context context;

    private String url = null;

    private List<String> cachedDocumentVector = null;

    public Document(String content) {
        this(content, new Context(null, null, null, null));
    }

    public Document(String content, Context context) {
        this.content = content;
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    public String getContent() {
        return content;
    }

    protected String getContentTruncated() {
        return getContentTruncated(CONTENT_TRUNCATED_MAX_LENGTH);
    }

    private String getContentTruncated(int maxLength) {
        boolean includeEllipses = this.content.length() > maxLength;
        return content.substring(0, Math.min(this.content.length(), maxLength)) + (includeEllipses ? "..." : "");
    }

    public void computeWordVector() {
        cachedDocumentVector = WordVector.getWordVector(this.content);
        cachedDocumentVector = WordVector.removeMostCommonWords(cachedDocumentVector);
    }

    public List<String> getWordVector() {
        return cachedDocumentVector;
    }

    @Override
    public String toString() {
        return "<" + Document.class.getSimpleName() + " content='" + getContentTruncated() + "'>";
    }

    public String getUrl() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String toTruncatedUrlString() {
        return StringUtils.truncateWithEllipse(this.getUrl(), CONTENT_TRUNCATED_MAX_LENGTH);
    }
}
