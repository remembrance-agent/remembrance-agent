package io.p13i.ra.utils;

import io.p13i.ra.models.Document;

import java.util.Arrays;
import java.util.List;

/**
 * @author Mohamed Guendouz
 */
public class TFIDFCalculator {
    /**
     * @param doc  list of strings
     * @param term String represents a term
     * @return term frequency of term in document
     */
    private static double tf(Document doc, String term) {
        double result = 0;
        List<String> wordVector = doc.getWordVector();
        for (String word : wordVector) {
            if (term.equalsIgnoreCase(word))
                result++;
        }
        return result / wordVector.size();
    }

    /**
     * @param docs list of list of strings represents the dataset
     * @param term String represents a term
     * @return the inverse term frequency of term in documents
     */
    private static double idf(Iterable<Document> docs, String term) {
        double n = 0;
        int N = 0;
        for (Document doc : docs) {
            List<String> wordVector = doc.getWordVector();
            for (String word : wordVector) {
                if (term.equalsIgnoreCase(word)) {
                    n++;
                    break;
                }
            }
            N++;
        }
        return Math.log(N / n);
    }

    /**
     * @param queryTerm term
     * @param document  a text document
     * @param documents all documents
     * @return the TF-IDF of term
     */
    public static double tfIdf(String queryTerm, Document document, Iterable<Document> documents) {
        return tf(document, queryTerm) * idf(documents, queryTerm);

    }

    public static void main(String[] args) {

        Document doc1 = new Document("a b c d e");
        Document doc2 = new Document("f g h i j");
        Document doc3 = new Document("a g c i e");

        List<Document> documents = Arrays.asList(doc1, doc2, doc3);

        double tfidf = TFIDFCalculator.tfIdf("a", doc1, documents);
        System.out.println("TF-IDF (a) = " + tfidf);
    }
}
