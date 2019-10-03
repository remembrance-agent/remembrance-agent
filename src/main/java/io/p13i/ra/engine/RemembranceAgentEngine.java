package io.p13i.ra.engine;

import io.p13i.ra.databases.IDocumentDatabase;
import io.p13i.ra.models.*;
import io.p13i.ra.similarity.DateSimilarityIndex;
import io.p13i.ra.similarity.StringSimilarityIndex;
import io.p13i.ra.utils.*;
import io.p13i.ra.cache.ICache;
import io.p13i.ra.cache.LimitedCapacityCache;

import java.util.*;
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
        if (query.getContentWindow().getWordVector().size() == 0) {
            return new ArrayList<>();
        }

        if (mSuggestionCache.hasKey(query)) {
            return mSuggestionCache.get(query);
        }

        // PriorityQueue sorts low -> high, we need to invert that
        PriorityQueue<ScoredSingleContentWindow> scoredSingleContentWindows = new PriorityQueue<>(query.getNumSuggestions(), Collections.reverseOrder());

        List<AbstractDocument> allDocuments = this.documentDatabase.getAllDocuments();
        for (AbstractDocument document : allDocuments) {
            for (SingleContentWindow window : document) {
                ScoredSingleContentWindow scoredDoc = Calculator.scoreQueryAgainstDocumentWindow(query, window, allDocuments, document);
                scoredSingleContentWindows.add(scoredDoc);
            }
        }

        Map<AbstractDocument, Tuple<Double, MultipleContentWindows>> documentsToScoresAndWindows = new LinkedHashMap<>();
        for (AbstractDocument document : allDocuments) {
            documentsToScoresAndWindows.put(document, new Tuple<>(0.0, new MultipleContentWindows()));
        }

        for (ScoredSingleContentWindow scoredWindow : scoredSingleContentWindows) {
            Tuple<Double, MultipleContentWindows> currentScoreAndWindows = documentsToScoresAndWindows.get(scoredWindow.getDocument());

            double currentScore = currentScoreAndWindows.x();
            MultipleContentWindows currentWindows = currentScoreAndWindows.y();

            if (!Double.isNaN(scoredWindow.getScore()) && scoredWindow.getScore() > 0.0) {
                currentScore += scoredWindow.getScore();
                currentWindows.getSingleWindows().add(scoredWindow.getWindow());
            }

            documentsToScoresAndWindows.put(scoredWindow.getDocument(), new Tuple<>(currentScore, currentWindows));
        }

        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(query.getNumSuggestions(), Collections.reverseOrder());

        for (Map.Entry<AbstractDocument, Tuple<Double, MultipleContentWindows>> abstractDocumentDoubleEntry : documentsToScoresAndWindows.entrySet()) {
            AbstractDocument document = abstractDocumentDoubleEntry.getKey();
            Double score = abstractDocumentDoubleEntry.getValue().x();
            MultipleContentWindows windows = abstractDocumentDoubleEntry.getValue().y();

            if (!score.isNaN() && score > 0.0) {
                scoredDocuments.add(new ScoredDocument(query, document, score, windows));
            }
        }

        while (scoredDocuments.size() > query.getNumSuggestions()) {
            scoredDocuments.remove();
        }

        List<ScoredDocument> scoredDocumentList = Arrays.asList(scoredDocuments.toArray(new ScoredDocument[0]));
        scoredDocumentList = scoredDocumentList.subList(0, Math.min(query.getNumSuggestions(), scoredDocumentList.size()));

        mSuggestionCache.put(query, scoredDocumentList);

        return scoredDocumentList;
    }


    /**
     * Scores queries against the document corpus
     */
    private static class Calculator {

        /**
         * These values bias the components of the query. Must add to 1.00
         */
        private static final double CONTENT_BIAS = 1.00;
        private static final double LOCATION_BIAS = 0.00;
        private static final double PERSON_BIAS = 0.00;
        private static final double SUBJECT_BIAS = 0.00;
        private static final double DATE_BIAS = 0.00;

        /**
         * Scores a query against all the documents in the data store
         *
         * @param query        the query
         * @param window       the window to check again
         * @param document     the document to check the query against
         * @param allDocuments the context of all documents
         * @return a score between 0.0 and 1.0
         */
        private static ScoredSingleContentWindow scoreQueryAgainstDocumentWindow(Query query, SingleContentWindow window, List<AbstractDocument> allDocuments, AbstractDocument document) {
            List<String> wordVector = query.getContentWindow().getWordVector();

            // Perform TFiDF on each word
            double wordScoreSum = 0.0;
            for (String word : wordVector) {

                double wordScore = TFIDFCalculator.tfIdf(word, window, allDocuments);

                if (Double.isNaN(wordScore)) {
                    // adding NaN to a double will cause the double to become NaN
                    continue;
                }

                wordScoreSum += (wordScore / (double) window.getWordVector().size());
            }


            // Normalize
            double contentScore = wordScoreSum / (double) wordVector.size();

            // Compute the contextual properties
            double locationScore = StringSimilarityIndex.calculate(query.getContext().getLocation(), document.getContext().getLocation());
            double personScore = StringSimilarityIndex.calculate(query.getContext().getPerson(), document.getContext().getPerson());
            double subjectScore = StringSimilarityIndex.calculate(query.getContext().getSubject(), document.getContext().getSubject());
            double dateScore = DateSimilarityIndex.calculate(query.getContext().getDate(), document.getContext().getDate());

            // Bias the terms
            double contentBiased = contentScore * CONTENT_BIAS;
            double locationBiased = locationScore * LOCATION_BIAS;
            double personBiased = personScore * PERSON_BIAS;
            double subjectBiased = subjectScore * SUBJECT_BIAS;
            double dateBiased = dateScore * DATE_BIAS;

            double score = contentBiased + locationBiased + personBiased + subjectBiased + dateBiased;

//            Assert.inRange(score, 0.0, 1.0);

            return new ScoredSingleContentWindow(query, score, document, window);
        }
    }
}
