package io.p13i.ra.models;

import java.util.Arrays;
import java.util.List;

public class Document {
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

    public void computeWordVector() {
        cachedDocumentVector = Arrays.asList(this.content
                .toLowerCase()
                // Remove non (alphanumeric, :, space) characters
                .replaceAll("[^a-zA-Z\\d\\s:]", "")
                // Split on colon or space
                .split("[ |:]"));
    }

    public List<String> getWordVector() {
        return cachedDocumentVector;
    }

    @Override
    public String toString() {
        return "<Document content='" + getContent() + "', word vector size=" + getWordVector().size() + ">";
    }
}
