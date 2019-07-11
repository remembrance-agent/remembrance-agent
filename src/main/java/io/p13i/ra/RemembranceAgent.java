package io.p13i.ra;

import io.p13i.ra.similarity.DocumentSimilarityIndex;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.models.ScoredDocument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class RemembranceAgent {
    private DocumentDatabase documentDatabase;

    public RemembranceAgent(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public List<Document> indexDocuments() {
        documentDatabase.loadDocuments();
        documentDatabase.indexDocuments();
        return this.documentDatabase.getAllDocuments();
    }

    public List<ScoredDocument> determineSuggestions(String query, Context queryContext, int numSuggestions) {
        // PriorityQueue
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(numSuggestions, Collections.reverseOrder());
        List<Document> allDocuments = this.documentDatabase.getAllDocuments();
        for (Document document : allDocuments) {
            double score = DocumentSimilarityIndex.compute(query, queryContext, document, allDocuments);
            scoredDocuments.add(new ScoredDocument(score, document));
        }

        // Get the top numSuggestions documents
        List<ScoredDocument> suggestedDocuments = new ArrayList<>(numSuggestions);
        while (suggestedDocuments.size() < numSuggestions && !scoredDocuments.isEmpty()) {
            ScoredDocument suggestedDocument = scoredDocuments.poll();
            if (!Double.isNaN(suggestedDocument.getScore()) && suggestedDocument.getScore() > 0.0) {
                suggestedDocuments.add(suggestedDocument);
            }
        }
        return suggestedDocuments;
    }

    public static void main(String[] args) {
        RemembranceAgent ra = new RemembranceAgent(new LocalDiskDocumentDatabase("/Users/p13i/Projects/glass-notes/sample-documents"));
        ra.indexDocuments();

        Context queryContext = new Context(null, null, null, null);
        final int numSuggestions = 2;
        List<ScoredDocument> suggestedDocuments = ra.determineSuggestions("c", queryContext, numSuggestions);

        for (ScoredDocument doc : suggestedDocuments) {
            System.out.println(doc.toString());
        }
    }
}
