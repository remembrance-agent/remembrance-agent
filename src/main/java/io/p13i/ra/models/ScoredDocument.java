package io.p13i.ra.models;

import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.StringUtils;
import io.p13i.ra.utils.TFIDFCalculator;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Used in the document ranking process to hold both the document and the corresponding score for a given query
 */
public final class ScoredDocument implements Comparable<ScoredDocument> {
    private final Query query;
    private final double score;
    private final AbstractDocument document;

    public ScoredDocument(Query query, double score, AbstractDocument document) {
        this.query = query;
        this.score = score;
        this.document = document;
    }

    public double getScore() {
        return score;
    }

    public AbstractDocument getDocument() {
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

    /**
     * @return a string displayable in the GUI
     */
    public String toShortString() {
        return String.format("%s [%s] -- %s", this.document.getDocumentTypeName(), StringUtils.truncateEndWithEllipse(getDocument().getContext().getSubject(), 20), ListUtils.toString(getMatchingTermsInDocument(5)));
    }

    /**
     * Gets the terms in the document matching the terms in the query, ranked by their TFs in TFiDF
     * @param maxCount limited to this count
     * @return the list of matching terms ranked
     */
    private List<String> getMatchingTermsInDocument(int maxCount) {
        List<String> queryWordVector = query.getWordVector();
        List<String> documentWordVector = document.getWordVector();
        List<String> intersection = ListUtils.intersection(queryWordVector, documentWordVector);
        return ListUtils.selectLargest(intersection, maxCount, ((queryTerm1, queryTerm2) -> {
            double queryTerm1TermFrequency = TFIDFCalculator.tf(getDocument(), queryTerm1);
            double queryTerm2TermFrequency = TFIDFCalculator.tf(getDocument(), queryTerm2);
            return Double.compare(queryTerm1TermFrequency, queryTerm2TermFrequency);
        }));
    }
}
