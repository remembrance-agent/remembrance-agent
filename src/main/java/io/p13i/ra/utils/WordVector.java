package io.p13i.ra.utils;

import io.p13i.ra.models.MultipleContentWindows;
import io.p13i.ra.models.SingleContentWindow;

import java.util.*;


/**
 * Wraps computations around converting strings to word vectors
 */
public class WordVector {
    private static final int MINIMUM_WORD_LENGTH = 2;

    // Based on https://en.wikipedia.org/wiki/Most_common_words_in_English#100_most_common_words
    private static final Set<String> MOST_COMMON_WORDS = new HashSet<>(Arrays.asList("is", "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"));

    /**
     * Processes the given raw document content into a multi-window object
     *
     * @param content the raw content
     * @return multiple windows for searching
     */
    public static MultipleContentWindows process(String content) {

        List<SingleContentWindow> resultantWindows = new LinkedList<>();
        for (SingleContentWindow window : WordVector.joinAdjacentSmallContentWindows(getWindowedContent(content), 100)) {
            SingleContentWindow stemmedWindow = WordVector.getStemmedSingleContentWindow(window);
            SingleContentWindow withoutCommonWords = WordVector.removeMostCommonWords(stemmedWindow);
            resultantWindows.add(withoutCommonWords);
        }

        return new MultipleContentWindows(resultantWindows);
    }

    /**
     * Breaks up a string into windows for improved indexing
     *
     * @param content the given string
     * @return a list of words
     */
    public static MultipleContentWindows getWindowedContent(String content) {
        // https://stackoverflow.com/a/454913/5071723
        String[] windowsAsStrings = StringUtils.lines(content);

        List<SingleContentWindow> windows = new ArrayList<>(windowsAsStrings.length);

        for (String windowAsString : windowsAsStrings) {
            windows.add(new SingleContentWindow(getWordVectorForWindow(windowAsString)));
        }

        return new MultipleContentWindows(windows);
    }

    /**
     * Gets the word vector from a string
     *
     * @param rawString the raw string content
     * @return a processed word vector
     */
    private static List<String> getWordVectorForWindow(String rawString) {
        List<String> wordVector = new ArrayList<>();
        boolean wordMode = true;

        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : rawString.toCharArray()) {
            if (CharacterUtils.isAlphanumeric(c)) {
                wordMode = true;
                stringBuilder.append(CharacterUtils.toUpperCase(c));
            } else {
                wordMode = false;
                // Only include words of a minimum size (to remove stray characters)
                if (stringBuilder.length() >= MINIMUM_WORD_LENGTH) {
                    wordVector.add(stringBuilder.toString().toUpperCase());
                    stringBuilder = new StringBuilder();
                }
            }
        }

        if (wordMode && stringBuilder.length() >= MINIMUM_WORD_LENGTH) {
            wordVector.add(stringBuilder.toString().toUpperCase());
        }

        return wordVector;
    }

    /**
     * Joins adjacent content windows that are smaller than the given word-count threshold into a single window
     *
     * @param windows the variable sized windows
     * @param wordThreshold the minimum number of words required per window
     * @return a new set of windows, each of which (except perhaps the last) have the minimum number of words requested
     */
    public static MultipleContentWindows joinAdjacentSmallContentWindows(MultipleContentWindows windows, int wordThreshold) {
        // Merge adjacent windows that are too small

        List<SingleContentWindow> originalWindows = windows.getSingleWindows();
        List<SingleContentWindow> adjustedWindows = new ArrayList<>(originalWindows.size());

        int N = originalWindows.size();

        int i = 0;

        while (i < N) {
            int wordCounter = 0;

            int j = i;

            do  {
                wordCounter += originalWindows.get(j).getWordVector().size();
                j++;
            } while (j < N && wordCounter < wordThreshold);

            List<String> adjustedWords = new ArrayList<>(wordCounter);
            for (int k = i; k < j && k < N; k++) {
                adjustedWords.addAll(originalWindows.get(k).getWordVector());
            }

            adjustedWindows.add(new SingleContentWindow(adjustedWords));

            i = j;
        }

        return new MultipleContentWindows(adjustedWindows);
    }

    /**
     * Stems a single window of content
     *
     * @param contentWindow the raw content window
     * @return a stemmed content window
     */
    public static SingleContentWindow getStemmedSingleContentWindow(SingleContentWindow contentWindow) {

        List<String> wordVector = new ArrayList<>();

        for (String word : contentWindow.getWordVector()) {
            String lowercased = word.toLowerCase();
            String stemmed = Stemmer.stem(lowercased);
            String uppercased = stemmed.toUpperCase();
            wordVector.add(uppercased);
        }

        return new SingleContentWindow(wordVector);
    }

    /**
     * Removes common words from {@code MOST_COMMON_WORDS}
     *
     * @param contentWindow the full vector in a window
     * @return a cleaner vector
     */
    public static SingleContentWindow removeMostCommonWords(SingleContentWindow contentWindow) {
        List<String> words = new LinkedList<>();

        for (String word : contentWindow.getWordVector()) {
            String lowercased = word.toLowerCase();
            if (!MOST_COMMON_WORDS.contains(lowercased)) {
                String uppercased = word.toUpperCase();
                words.add(uppercased);
            }
        }

        return new SingleContentWindow(words);
    }
}
