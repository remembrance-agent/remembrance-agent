import io.p13i.ra.databases.AbstractDocumentDatabase;
import io.p13i.ra.databases.in_memory.InMemoryDocument;
import io.p13i.ra.databases.in_memory.InMemoryDocumentDatabase;
import io.p13i.ra.engine.IRemembranceAgentEngine;
import io.p13i.ra.engine.RemembranceAgentEngine;
import io.p13i.ra.models.Context;
import io.p13i.ra.models.Query;
import io.p13i.ra.models.ScoredDocument;
import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

public class RemembranceAgentEngineTests {

    @Test
    public void testRemembranceAgentEngine() {
        List<InMemoryDocument> documents = new LinkedList<InMemoryDocument>() {{
            add(new InMemoryDocument("abc def ghi", Context.NULL));
            add(new InMemoryDocument("def ghi jkl", Context.NULL));
            add(new InMemoryDocument("mno pqr stu", Context.NULL));
        }};

        AbstractDocumentDatabase documentDatabase = new InMemoryDocumentDatabase(documents);

        IRemembranceAgentEngine engine = new RemembranceAgentEngine(documentDatabase) {{
            loadDocuments();
            indexDocuments();
        }};

        Query query = new Query("def ghi jkl", Context.NULL, 3) {{
            index();
        }};

        List<ScoredDocument> scoredDocuments = engine.determineSuggestions(query);

        Assert.assertEquals(2, scoredDocuments.size());

        Assert.assertEquals("def ghi jkl", scoredDocuments.get(0).getDocument().getContent());
        Assert.assertEquals("abc def ghi", scoredDocuments.get(1).getDocument().getContent());

        Assert.assertTrue(scoredDocuments.get(0).getScore() > scoredDocuments.get(1).getScore());
    }
}
