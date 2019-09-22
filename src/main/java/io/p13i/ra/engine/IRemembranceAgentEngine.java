package io.p13i.ra.engine;

import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import io.p13i.ra.models.ScoredSingleContentWindow;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Core implementation of a Remembrance Agent.
 * Follows the specifications set out by Rhodes/Starner.
 * See http://ra.p13i.io
 *
 * @author Pramod Kotipalli
 */
public interface IRemembranceAgentEngine {

    /**
     * Loads all documents from their database
     */
    void loadDocuments();

    /**
     * Indexes all documents
     */
    void indexDocuments();

    /**
     * Determines the suggestions for a given query in this Remembrance Agent
     *
     * @param query the query
     * @return scored documents limited to the number of suggestions requested in the query, ordered by score, descending
     */
    List<ScoredDocument> determineSuggestions(Query query);
}
