package io.p13i.ra.utils;

import io.p13i.ra.cache.Cache;
import io.p13i.ra.cache.AbstractCache;
import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.models.SingleContentWindow;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Computes TFiDF
 *
 * @author Mohamed Guendouz
 * @author Pramod Kotipalli (@p13i)
 */
public class TFIDFCalculator {

    private static AbstractCache<Tuple<SingleContentWindow, String>, Double> tfCache = new Cache<>();
    private static AbstractCache<Tuple<Iterable<AbstractDocument>, String>, Double> idfCache = new Cache<>();

    /**
     * @param doc  list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    public static double tf(SingleContentWindow doc, String term) {
        int result = 0;
        List<String> wordVector = doc.getWordVector();
        for (String word : wordVector) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / (double) wordVector.size();
    }

    /**
     * @param docs list of list of strings represents the dataset
     * @param term String represents a term
     * @return the inverse term frequency of term in documents
     */
    private static double idf(Iterable<AbstractDocument> docs, String term) {
        int n = 0;  // total number of number of documents that contains this word
        int N = 0;  // number of documents

        for (AbstractDocument doc : docs) {
            for (SingleContentWindow window : doc.getContentWindows()) {
                List<String> wordVector = window.getWordVector();
                for (String word : wordVector) {
                    if (term.equalsIgnoreCase(word)) {
                        n++;
                        break;
                    }
                }
            }

            N++;
        }

        if (N == n) {
            return 1.0;
        }

        return Math.log((double) N / (double) n);
    }

    /**
     * Computes the TFiDF of a query for a given document
     *
     * @param queryTerm term
     * @param window  a text document
     * @param documents all documents
     * @return the TF-IDF of term
     */
    public static double tfIdf(final String queryTerm, final SingleContentWindow window, final Iterable<AbstractDocument> documents) {
        double tf = tfCache.get(new Tuple<>(window, queryTerm), new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return tf(window, queryTerm);
            }
        });
        double idf = idfCache.get(new Tuple<>(documents, queryTerm), new Callable<Double>() {
            @Override
            public Double call() throws Exception {
                return idf(documents, queryTerm);
            }
        });
        return tf * idf;
    }
}
