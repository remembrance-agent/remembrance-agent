package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;
import java.util.Objects;

/**
 * Wraps a query to the RA
 */
public class Query implements IRequiresIndexing {
    private final String query;
    private final Context context;
    private final int numSuggestions;
    private ContentWindow cachedContentWindow;

    public Query(String query, Context context, int numSuggestions) {
        this.query = query;
        this.context = context;
        this.numSuggestions = numSuggestions;
    }

    public String getQuery() {
        return query;
    }

    public Context getContext() {
        return context;
    }

    public int getNumSuggestions() {
        return numSuggestions;
    }

    public ContentWindow getContentWindow() {
        return cachedContentWindow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.query, this.context, this.numSuggestions);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Query)) {
            return false;
        }

        Query other = (Query) obj;

        return this.query.equals(other.query) &&
                this.context.equals(other.context) &&
                this.numSuggestions == other.numSuggestions;
    }

    @Override
    public void index() {
        this.cachedContentWindow = WordVector.process(getQuery());
    }
}
