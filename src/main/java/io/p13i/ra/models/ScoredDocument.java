package io.p13i.ra.models;

import io.p13i.ra.utils.ListUtils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Used in the document ranking process to hold both the document and the corresponding score for a given query
 */
public final class ScoredDocument implements Comparable<ScoredDocument> {
    private Query query;
    private double score;
    private Document document;

    public ScoredDocument(Query query, double score, Document document) {
        this.query = query;
        this.score = score;
        this.document = document;
    }

    public double getScore() {
        return score;
    }

    public Document getDocument() {
        return document;
    }

    @Override
    public int compareTo(ScoredDocument o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "<ScoredDocument score=" + new DecimalFormat("#0.0000").format(getScore()) + ", document=" + getDocument().toString() + ">";
    }

    public String toShortString() {
        return String.format("(%04f) [%s]: %s", getScore(), getDocument().toTruncatedUrlString(), ListUtils.asString(getMatchingTermsInDocument()));
    }

    public List<String> getMatchingTermsInDocument() {
        List<String> queryWordVector = query.getWordVector();
        List<String> documentWordVector = document.getWordVector();
        return ListUtils.intersection(queryWordVector, documentWordVector);
    }
}
