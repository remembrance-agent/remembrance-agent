package io.p13i.ra.models;

import io.p13i.ra.utils.WordVector;

import java.util.List;

public class Query {
    private final String query;
    private final Context context;
    private final int numSuggestions;

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

    public List<String> getWordVector() {
        List<String> wordVector = WordVector.getWordVector(getQuery());
        wordVector = WordVector.removeMostCommonWords(wordVector);
        return wordVector;
    }
}
