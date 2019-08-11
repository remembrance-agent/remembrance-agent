package io.p13i.ra.engine;

import io.p13i.ra.databases.DocumentDatabase;
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

    private final DocumentDatabase documentDatabase;

    public RemembranceAgentEngine(DocumentDatabase documentDatabase) {
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

        // PriorityQueue sorts low -> high, we need to invert that
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(query.getNumSuggestions(), Collections.reverseOrder());

        ConfusionMatrix confusionMatrix = new ConfusionMatrix();
        List<AbstractDocument> allDocuments = this.documentDatabase.getAllDocuments();
        LOGGER.info("Searching " + allDocuments.size() + " documents for '" + query.getQuery() + "'");
        for (AbstractDocument document : allDocuments) {
            ScoredDocument scoredDoc = scoreQueryAgainstDocuments(query, document, allDocuments, confusionMatrix);
            scoredDocuments.add(scoredDoc);
        }

//        LOGGER.info("\n" + confusionMatrix.toString());

        // Get the top numSuggestions documents
        List<ScoredDocument> suggestedDocuments = new ArrayList<>(query.getNumSuggestions());
        while (suggestedDocuments.size() < query.getNumSuggestions() && !scoredDocuments.isEmpty()) {
            ScoredDocument suggestedDocument = scoredDocuments.poll();
            if (!Double.isNaN(suggestedDocument.getScore()) && suggestedDocument.getScore() > 0.0) {
                suggestedDocuments.add(suggestedDocument);
            }
        }
        return suggestedDocuments;
    }

    private static final double CONTENT_BIAS = 1.00;
    private static final double LOCATION_BIAS = 0.00;
    private static final double PERSON_BIAS = 0.00;
    private static final double SUBJECT_BIAS = 0.00;
    private static final double DATE_BIAS = 0.00;

    static {
        Assert.equal(CONTENT_BIAS + LOCATION_BIAS + PERSON_BIAS + SUBJECT_BIAS + DATE_BIAS, 1.0);
    }

    private ScoredDocument scoreQueryAgainstDocuments(Query query, AbstractDocument document, List<AbstractDocument> allDocuments, ConfusionMatrix confusionMatrix) {
        List<String> wordVector = query.getWordVector();

        double wordScoreSum = 0.0;
        for (String word : wordVector) {

            double wordScore = TFIDFCalculator.tfIdf(word, document, allDocuments);

            confusionMatrix.add(word, document.getContext().getSubject(), wordScore);

            if (Double.isNaN(wordScore)) {
                // adding NaN to a double will cause the double to become NaN
                continue;
            }

            wordScoreSum += wordScore;
        }


        // Normalize
        double contentScore = wordScoreSum / (double) wordVector.size();

        StringSimilarityIndex stringSimilarityIndex = new StringSimilarityIndex();
        DateSimilarityIndex dateSimilarityIndex = new DateSimilarityIndex();

        double locationScore = stringSimilarityIndex.calculate(query.getContext().getLocation(), document.getContext().getLocation());
        double personScore = stringSimilarityIndex.calculate(query.getContext().getPerson(), document.getContext().getPerson());
        double subjectScore = stringSimilarityIndex.calculate(query.getContext().getSubject(), document.getContext().getSubject());
        double dateScore = dateSimilarityIndex.calculate(query.getContext().getDate(), document.getContext().getDate());

        double contentBiased = contentScore * CONTENT_BIAS;
        double locationBiased = locationScore * LOCATION_BIAS;
        double personBiased = personScore * PERSON_BIAS;
        double subjectBiased = subjectScore * SUBJECT_BIAS;
        double dateBiased = dateScore * DATE_BIAS;

        double score = contentBiased + locationBiased + personBiased + subjectBiased + dateBiased;

        return new ScoredDocument(query, score, document);
    }
}
