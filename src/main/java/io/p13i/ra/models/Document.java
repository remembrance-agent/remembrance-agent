package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;

public class Document {
    public final int CONTENT_TRUNCATED_MAX_LENGTH = 5;

    protected Context context;
    protected String content;

    private List<String> cachedDocumentVector = null;

    public Document(String content) {
        this.content = content;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public String getContent() {
        return content;
    }

    public String getContentTruncated() {
        boolean includeEllipses = this.content.length() > CONTENT_TRUNCATED_MAX_LENGTH;
        return content.substring(0, Math.min(content.length(), CONTENT_TRUNCATED_MAX_LENGTH)) + (includeEllipses ? "..." : "");
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
        return "<" + Document.class.getSimpleName() + " content='" + getContentTruncated() + "', word vector size=" + getWordVector().size() + ">";
    }
}
