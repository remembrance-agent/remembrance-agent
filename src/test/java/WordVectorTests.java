import io.p13i.ra.models.MultipleContentWindows;
import io.p13i.ra.models.SingleContentWindow;
import io.p13i.ra.utils.WordVector;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class WordVectorTests {

    @Test
    public void testProcess() {
        MultipleContentWindows windows = WordVector.getWindowedContent("The quick\nbrown fox\njumped over\nthe lazy dog.");
        assertEquals(4, windows.getSingleWindows().size());

        {
            SingleContentWindow stemmedWindow = WordVector.getStemmedSingleContentWindow(windows.getSingleWindows().get(0));
            assertEquals(Arrays.asList("THE", "QUICK"), stemmedWindow.getWordVector());
            SingleContentWindow noCommonWordsWindow = WordVector.removeMostCommonWords(stemmedWindow);
            assertEquals(Arrays.asList("QUICK"), noCommonWordsWindow.getWordVector());
        }

        {
            SingleContentWindow stemmedWindow = WordVector.getStemmedSingleContentWindow(windows.getSingleWindows().get(1));
            assertEquals(Arrays.asList("BROWN", "FOX"), stemmedWindow.getWordVector());
            SingleContentWindow noCommonWordsWindow = WordVector.removeMostCommonWords(stemmedWindow);
            assertEquals(Arrays.asList("BROWN", "FOX"), noCommonWordsWindow.getWordVector());
        }

        {
            SingleContentWindow stemmedWindow = WordVector.getStemmedSingleContentWindow(windows.getSingleWindows().get(2));
            assertEquals(Arrays.asList("JUMP", "OVER"), stemmedWindow.getWordVector());
            SingleContentWindow noCommonWordsWindow = WordVector.removeMostCommonWords(stemmedWindow);
            assertEquals(Arrays.asList("JUMP"), noCommonWordsWindow.getWordVector());
        }

        {
            SingleContentWindow stemmedWindow = WordVector.getStemmedSingleContentWindow(windows.getSingleWindows().get(3));
            assertEquals(Arrays.asList("THE", "LAZI", "DOG"), stemmedWindow.getWordVector());
            SingleContentWindow noCommonWordsWindow = WordVector.removeMostCommonWords(stemmedWindow);
            assertEquals(Arrays.asList("LAZI", "DOG"), noCommonWordsWindow.getWordVector());
        }
    }

    @Test
    public void test_joinAdjacentSmallContentWindows() {
        {
            MultipleContentWindows windows = WordVector.getWindowedContent("The quick\nbrown fox\njumped over\nthe lazy dog.");
            windows = WordVector.joinAdjacentSmallContentWindows(windows, 5);

            assertEquals(2, windows.getSingleWindows().size());

            assertEquals(Arrays.asList("THE", "QUICK", "BROWN", "FOX", "JUMPED", "OVER"), windows.getSingleWindows().get(0).getWordVector());
            assertEquals(Arrays.asList("THE", "LAZY", "DOG"), windows.getSingleWindows().get(1).getWordVector());
        }

        {
            MultipleContentWindows windows = WordVector.getWindowedContent("The quick\nbrown fox\njumped over the\nlazy dog.");
            windows = WordVector.joinAdjacentSmallContentWindows(windows, 3);

            assertEquals(3, windows.getSingleWindows().size());

            assertEquals(Arrays.asList("THE", "QUICK", "BROWN", "FOX"), windows.getSingleWindows().get(0).getWordVector());
            assertEquals(Arrays.asList("JUMPED", "OVER", "THE"), windows.getSingleWindows().get(1).getWordVector());
            assertEquals(Arrays.asList("LAZY", "DOG"), windows.getSingleWindows().get(2).getWordVector());
        }
    }

}