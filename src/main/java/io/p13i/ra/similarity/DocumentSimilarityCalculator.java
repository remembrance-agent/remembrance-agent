package io.p13i.ra.similarity;

import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.utils.Assert;
import io.p13i.ra.utils.TFIDFCalculator;

import java.util.List;

public class DocumentSimilarityCalculator {
    private static final double CONTENT_BIAS = 0.95;
    private static final double LOCATION_BIAS = 0.01;
    private static final double PERSON_BIAS = 0.01;
    private static final double SUBJECT_BIAS = 0.02;
    private static final double DATE_BIAS = 0.01;

    static {
        Assert.equal(CONTENT_BIAS + LOCATION_BIAS + PERSON_BIAS + SUBJECT_BIAS + DATE_BIAS, 1.0);
    }

    public static double compute(String query, Context queryContext, Document document, List<Document> allDocuments) {
        double contentScore = TFIDFCalculator.tfIdf(query, document, allDocuments);

        StringSimilarityIndex stringSimilarityIndex = new StringSimilarityIndex();
        DateSimilarityIndex dateSimilarityIndex = new DateSimilarityIndex();

        double locationScore = stringSimilarityIndex.calculate(queryContext.getLocation(), document.getContext().getLocation());
        double personScore = stringSimilarityIndex.calculate(queryContext.getPerson(), document.getContext().getPerson());
        double subjectScore = stringSimilarityIndex.calculate(queryContext.getSubject(), document.getContext().getSubject());
        double dateScore = dateSimilarityIndex.calculate(queryContext.getDate(), document.getContext().getDate());

        double contentBiased = contentScore * CONTENT_BIAS;
        double locationBiased = locationScore * LOCATION_BIAS;
        double personBiased = personScore * PERSON_BIAS;
        double subjectBiased = subjectScore * SUBJECT_BIAS;
        double dateBiased = dateScore * DATE_BIAS;

        return contentBiased + locationBiased + personBiased + subjectBiased + dateBiased;
    }
}
