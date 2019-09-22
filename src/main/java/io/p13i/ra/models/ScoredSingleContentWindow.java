package io.p13i.ra.models;

import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.StringUtils;
import io.p13i.ra.utils.TFIDFCalculator;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Used in the document ranking process to hold both the document and the corresponding score for a given query
 */
public final class ScoredSingleContentWindow implements Comparable<ScoredSingleContentWindow> {
    private final Query query;
    private final double score;
    private final AbstractDocument document;
    private final SingleContentWindow window;

    public ScoredSingleContentWindow(Query query, double score, AbstractDocument document, SingleContentWindow window) {
        this.query = query;
        this.score = score;
        this.document = document;
        this.window = window;
    }

    public double getScore() {
        return score;
    }

    public AbstractDocument getDocument() {
        return document;
    }

    public Query getQuery() {
        return this.query;
    }

    public SingleContentWindow getWindow() {
        return this.window;
    }

    @Override
    public int compareTo(ScoredSingleContentWindow o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "<ScoredSingleContentWindow score=" + new DecimalFormat("#0.0000").format(getScore()) + ", document=" + getDocument().toString() + ">";
    }
}
