package io.p13i.ra.models;

import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.TFIDFCalculator;

import java.text.DecimalFormat;
import java.util.Comparator;
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
        List<String> intersection = ListUtils.intersection(queryWordVector, documentWordVector);
        return ListUtils.selectLargest(intersection, 3, new Comparator<String>() {
            @Override
            public int compare(String queryTerm1, String queryTerm2) {
                double queryTerm1TermFrequency = TFIDFCalculator.tf(getDocument(), queryTerm1);
                double queryTerm2TermFrequency = TFIDFCalculator.tf(getDocument(), queryTerm2);
                if (queryTerm1TermFrequency > queryTerm2TermFrequency) {
                    return 1;
                } else if (queryTerm1TermFrequency < queryTerm2TermFrequency) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
    }
}
