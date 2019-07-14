package io.p13i.ra.engine;

import io.p13i.ra.models.Context;
import io.p13i.ra.models.Document;
import io.p13i.ra.databases.DocumentDatabase;
import io.p13i.ra.databases.localdisk.LocalDiskDocumentDatabase;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.utils.LoggerUtils;
import io.p13i.ra.utils.ResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Logger;


/**
 * Core RA implementation and wrapper functions
 */
public class RemembranceAgentEngine {
    private static final Logger LOGGER = LoggerUtils.getLogger(RemembranceAgentEngine.class);

    private DocumentDatabase documentDatabase;

    public RemembranceAgentEngine(DocumentDatabase documentDatabase) {
        this.documentDatabase = documentDatabase;
    }

    public List<Document> indexDocuments() {
        this.documentDatabase.loadDocuments();
        this.documentDatabase.indexDocuments();
        return this.documentDatabase.getAllDocuments();
    }

    public List<ScoredDocument> determineSuggestions(Query query) {
        // PriorityQueue sorts low -> high, we need to invert that
        PriorityQueue<ScoredDocument> scoredDocuments = new PriorityQueue<>(query.getNumSuggestions(), Collections.reverseOrder());

        ConfusionMatrix confusionMatrix = new ConfusionMatrix();
        List<Document> allDocuments = this.documentDatabase.getAllDocuments();
        for (Document document : allDocuments) {
            ScoredDocument scoredDoc = RemembranceAgentSuggestionCalculator.compute(query, document, allDocuments, confusionMatrix);
            scoredDocuments.add(scoredDoc);
        }

        LOGGER.info("\n" + confusionMatrix.toString());

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

    public static void main(String[] args) {
        String directoryPath = ResourceUtil.getResourcePath(RemembranceAgentEngine.class, "sample-documents");
        RemembranceAgentEngine ra = new RemembranceAgentEngine(new LocalDiskDocumentDatabase(directoryPath));
        ra.indexDocuments();

        Context queryContext = new Context(null, null, null, null);
        final int numSuggestions = 2;
        Query query = new Query("jesus", queryContext, numSuggestions);
        List<ScoredDocument> suggestedDocuments = ra.determineSuggestions(query);

        for (ScoredDocument doc : suggestedDocuments) {
            System.out.println(doc.toString());
        }
    }
}
