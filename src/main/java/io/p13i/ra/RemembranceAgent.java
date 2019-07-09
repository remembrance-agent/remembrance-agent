package io.p13i.ra;

import io.p13i.ra.models.Document;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.tfidf.TFIDFCalculator;
import io.p13i.ra.tfidf.TFIDFScoredDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class RemembranceAgent {
    private DocumentDatabase documentDatabase;

    public RemembranceAgent(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public void indexDocuments() {
        documentDatabase.load();
        documentDatabase.index();
    }

    public List<TFIDFScoredDocument> determineSuggestions(String query, int numSuggestions) {
        PriorityQueue<TFIDFScoredDocument> scoredDocuments = new PriorityQueue<>(numSuggestions, Collections.reverseOrder());
        List<Document> allDocuments = this.documentDatabase.getAllDocuments();
        for (Document document : allDocuments) {
            double score = TFIDFCalculator.tfIdf(document, allDocuments, query);
            scoredDocuments.add(new TFIDFScoredDocument(score, document));
        }
        List<TFIDFScoredDocument> suggestedDocuments = new ArrayList<>(numSuggestions);
        while (suggestedDocuments.size() < numSuggestions && !scoredDocuments.isEmpty()) {
            suggestedDocuments.add(scoredDocuments.poll());
        }
        return suggestedDocuments;
    }

    public static void main(String[] args) {
        DocumentDatabase documentDatabase = new LocalDiskDocumentDatabase("/Users/p13i/Projects/glass-notes/sample-documents");
        RemembranceAgent ra = new RemembranceAgent(documentDatabase);
        ra.indexDocuments();
        List<TFIDFScoredDocument> suggestedDocuments = ra.determineSuggestions("a", 2);
        for (TFIDFScoredDocument doc : suggestedDocuments) {
            System.out.println(doc.toString());
        }
    }
}
