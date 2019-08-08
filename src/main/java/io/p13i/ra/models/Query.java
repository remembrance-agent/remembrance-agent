package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;

public class Query {
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
}
