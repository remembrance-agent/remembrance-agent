package io.p13i.ra.models;

import java.text.DecimalFormat;

public final class ScoredDocument implements Comparable<ScoredDocument> {
    private double score;
    private Document document;

    public ScoredDocument(double score, Document document) {
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
}
