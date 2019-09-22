package io.p13i.ra.models;

import io.p13i.ra.utils.ListUtils;
import io.p13i.ra.utils.StringUtils;
import io.p13i.ra.utils.TFIDFCalculator;

import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class ScoredDocument implements Comparable<ScoredDocument> {
    private Query query;
    private AbstractDocument document;
    private double score;
    private MultipleContentWindows multipleContentWindows;

    public ScoredDocument(Query query, AbstractDocument document, double score, MultipleContentWindows multipleContentWindows) {
        this.query = query;
        this.document = document;
        this.score = score;
        this.multipleContentWindows = multipleContentWindows;
    }

    public double getScore() {
        return score;
    }

    public AbstractDocument getDocument() {
        return document;
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
            SingleContentWindow singleContentWindow = multipleContentWindows.asSingleContentWindow();
            double queryTerm1TermFrequency = TFIDFCalculator.tf(singleContentWindow, queryTerm1);
            double queryTerm2TermFrequency = TFIDFCalculator.tf(singleContentWindow, queryTerm2);
            return Double.compare(queryTerm1TermFrequency, queryTerm2TermFrequency);
        }));
    }

    private Query getQuery() {
        return this.query;
    }

    @Override
    public int compareTo(ScoredDocument o) {
        return Double.compare(this.score, o.score);
    }
}
