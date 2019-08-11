package io.p13i.ra.engine;

import io.p13i.ra.models.AbstractDocument;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;

import java.util.List;

public interface IRemembranceAgentEngine {
    List<AbstractDocument> indexDocuments();

    List<ScoredDocument> determineSuggestions(Query query);
}
