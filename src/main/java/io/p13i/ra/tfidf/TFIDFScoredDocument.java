package io.p13i.ra.tfidf;

import io.p13i.ra.models.Document;

public class TFIDFScoredDocument implements Comparable<TFIDFScoredDocument> {
    private double score;
    private Document document;

    public TFIDFScoredDocument(double score, Document document) {
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
    public int compareTo(TFIDFScoredDocument o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "<TFIDFScoredDocument score=" + getScore() + ", document=" + getDocument().toString() + ">";
    }
}
