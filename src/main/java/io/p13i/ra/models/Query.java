package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;
import java.util.Objects;

public final class Query {
    private final String query;
    private final Context context;
    private final int numSuggestions;
    private List<String> cachedWordVector;

    public Query(String query, Context context, int numSuggestions) {
        this.query = query;
        this.context = context;
        this.numSuggestions = numSuggestions;
        this.computeWordVector();
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

    public void computeWordVector() {
        this.cachedWordVector = WordVector.getWordVector(getQuery());
        this.cachedWordVector = WordVector.removeMostCommonWords(cachedWordVector);
    }

    public List<String> getWordVector() {
        return cachedWordVector;
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
}
