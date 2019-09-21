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

    /**
     * @return a string displayable in the GUI
     */
    public String toShortString() {
        String subjectTruncated = StringUtils.truncateEndWithEllipse(getDocument().getContext().getSubject(), 40);
        String matchingTerms = ListUtils.toString(getMatchingTermsInDocument(5), "", "", ", ", "");
        return String.format("[%s]: %s", subjectTruncated, matchingTerms);
    }

    /**
     * Gets the terms in the document matching the terms in the query, ranked by their TFs in TFiDF
     *
     * @param maxCount limited to this count
     * @return the list of matching terms ranked
     */
    private List<String> getMatchingTermsInDocument(int maxCount) {
        List<String> queryWordVector = getQuery().getContentWindow().getWordVector();
        List<String> documentWordVector = getDocument().getContentWindows().asSingleContentWindow().getWordVector();
        List<String> intersection = ListUtils.intersection(queryWordVector, documentWordVector);
        return ListUtils.selectLargest(intersection, maxCount, ((queryTerm1, queryTerm2) -> {
            double queryTerm1TermFrequency = TFIDFCalculator.tf(getWindow(), queryTerm1);
            double queryTerm2TermFrequency = TFIDFCalculator.tf(getWindow(), queryTerm2);
            return Double.compare(queryTerm1TermFrequency, queryTerm2TermFrequency);
        }));
    }
}
