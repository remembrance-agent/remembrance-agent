package io.p13i.ra.engine;

import io.p13i.ra.RemembranceAgentClient;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.similarity.DateSimilarityIndex;
import io.p13i.ra.similarity.StringSimilarityIndex;
import io.p13i.ra.utils.*;

import java.util.List;
import java.util.logging.Logger;

public class RemembranceAgentSuggestionCalculator {

    private static final Logger LOGGER = LoggerUtils.getLogger( RemembranceAgentClient.class);

    private static final double CONTENT_BIAS = 1.00;
    private static final double LOCATION_BIAS = 0.00;
    private static final double PERSON_BIAS = 0.00;
    private static final double SUBJECT_BIAS = 0.00;
    private static final double DATE_BIAS = 0.00;

    static {
        Assert.equal(CONTENT_BIAS + LOCATION_BIAS + PERSON_BIAS + SUBJECT_BIAS + DATE_BIAS, 1.0);
    }

    public static ScoredDocument compute(Query query, Document document, List<Document> allDocuments) {
        List<String> wordVector = query.getWordVector();

        LOGGER.info("Computed query word vector: '" + ListUtils.asString(wordVector) + "'");

        double wordScoreSum = 0.0;
        for (String word : wordVector) {
            double wordScore = TFIDFCalculator.tfIdf(word, document, allDocuments);
            LOGGER.info(String.format("Ranked word '%s' as %04f in %s", word, wordScore, document.getContext().getSubject()));

            if (Double.isNaN(wordScore)) {
                wordScore = 0.0;
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

        return new ScoredDocument(score, document);
    }
}
