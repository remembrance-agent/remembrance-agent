package io.p13i.ra.engine;

import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;


/**
 * Core RA implementation and wrapper functions
 */
public class RemembranceAgentEngine {
    private DocumentDatabase documentDatabase;

    public RemembranceAgentEngine(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public List<Document> indexDocuments() {
        this.documentDatabase.loadDocuments();
        this.documentDatabase.indexDocuments();
        return this.documentDatabase.getAllDocuments();
    }

    public List<ScoredDocument> determineSuggestions(String query, Context queryContext, int numSuggestions) {
        // PriorityQueue
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(numSuggestions, Collections.reverseOrder());
        List<Document> allDocuments = this.documentDatabase.getAllDocuments();
        for (Document document : allDocuments) {
            ScoredDocument scoredDoc = RemembranceAgentSuggestionCalculator.compute(query, queryContext, document, allDocuments);
            scoredDocuments.add(scoredDoc);
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
        String directoryPath = ResourceUtil.getResourcePath(RemembranceAgentEngine.class, "sample-documents");
        RemembranceAgentEngine ra = new RemembranceAgentEngine(new LocalDiskDocumentDatabase(directoryPath));
        ra.indexDocuments();

        Context queryContext = new Context(null, null, null, null);
        final int numSuggestions = 2;
        List<ScoredDocument> suggestedDocuments = ra.determineSuggestions("c", queryContext, numSuggestions);

        for (ScoredDocument doc : suggestedDocuments) {
            System.out.println(doc.toString());
        }
    }
}
