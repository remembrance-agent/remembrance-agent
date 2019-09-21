import io.p13i.ra.utils.Stemmer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StemmerTests {

    @Test
    public void testStemmer() {
        assertEquals("run", Stemmer.stem("running"));
    }
}