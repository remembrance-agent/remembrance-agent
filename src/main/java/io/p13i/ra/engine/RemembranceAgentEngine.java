package io.p13i.ra.engine;

import com.google.inject.Inject;
import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.models.*;
import io.p13i.ra.similarity.DateSimilarityIndex;
import io.p13i.ra.similarity.StringSimilarityIndex;
import io.p13i.ra.utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;


/**
 * Core RA implementation and wrapper functions
 */
public class RemembranceAgentEngine implements IRemembranceAgentEngine {
    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentEngine.class);

    private final IDocumentDatabase documentDatabase;
    private ICache<Query, List<ScoredDocument>> mSuggestionCache = new LimitedCapacityCache<>(128);

    public RemembranceAgentEngine(IDocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    @Override
    public void loadDocuments() {
        this.documentDatabase.loadDocuments();
    }

    @Override
    public void indexDocuments() {
        this.documentDatabase.indexDocuments();
    }

    @Override
    public List<ScoredDocument> determineSuggestions(Query query) {
        if (query.getWordVector().size() == 0) {
            return new ArrayList<>();
        }

        if (mSuggestionCache.hasKey(query)) {
            return mSuggestionCache.get(query);
        }

        // PriorityQueue sorts low -> high, we need to invert that
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(query.getNumSuggestions(), Collections.reverseOrder());

        List<AbstractDocument> allDocuments = this.documentDatabase.getAllDocuments();
        LOGGER.info("Searching " + allDocuments.size() + " documents for '" + query.getQuery() + "'");
        for (AbstractDocument document : allDocuments) {
            ScoredDocument scoredDoc = scoreQueryAgainstDocuments(query, document, allDocuments);
            scoredDocuments.add(scoredDoc);
        }

        // Get the top numSuggestions documents
        List<ScoredDocument> suggestedDocuments = new ArrayList<>(query.getNumSuggestions());
        while (suggestedDocuments.size() < query.getNumSuggestions() && !scoredDocuments.isEmpty()) {
            ScoredDocument suggestedDocument = scoredDocuments.poll();
            if (!Double.isNaN(suggestedDocument.getScore()) && suggestedDocument.getScore() > 0.0) {
                suggestedDocuments.add(suggestedDocument);
            }
        }

        mSuggestionCache.put(query, suggestedDocuments);

        return suggestedDocuments;
    }

    private static final double CONTENT_BIAS = 1.00;
    private static final double LOCATION_BIAS = 0.00;
    private static final double PERSON_BIAS = 0.00;
    private static final double SUBJECT_BIAS = 0.00;
    private static final double DATE_BIAS = 0.00;

    static {
        Assert.almostEqual(CONTENT_BIAS + LOCATION_BIAS + PERSON_BIAS + SUBJECT_BIAS + DATE_BIAS, 1.0);
    }

    private ScoredDocument scoreQueryAgainstDocuments(Query query, AbstractDocument document, List<AbstractDocument> allDocuments) {
        List<String> wordVector = query.getWordVector();

        double wordScoreSum = 0.0;
        for (String word : wordVector) {

            double wordScore = TFIDFCalculator.tfIdf(word, document, allDocuments);

            if (Double.isNaN(wordScore)) {
                // adding NaN to a double will cause the double to become NaN
                continue;
            }

            wordScoreSum += wordScore;
        }


        // Normalize
        double contentScore = wordScoreSum / (double) wordVector.size();


        double locationScore = StringSimilarityIndex.calculate(query.getContext().getLocation(), document.getContext().getLocation());
        double personScore = StringSimilarityIndex.calculate(query.getContext().getPerson(), document.getContext().getPerson());
        double subjectScore = StringSimilarityIndex.calculate(query.getContext().getSubject(), document.getContext().getSubject());
        double dateScore = DateSimilarityIndex.calculate(query.getContext().getDate(), document.getContext().getDate());

        double contentBiased = contentScore * CONTENT_BIAS;
        double locationBiased = locationScore * LOCATION_BIAS;
        double personBiased = personScore * PERSON_BIAS;
        double subjectBiased = subjectScore * SUBJECT_BIAS;
        double dateBiased = dateScore * DATE_BIAS;

        double score = contentBiased + locationBiased + personBiased + subjectBiased + dateBiased;

        return new ScoredDocument(query, score, document);
    }
}
