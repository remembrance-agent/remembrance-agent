package io.p13i.ra.utils;

import io.p13i.ra.models.MultipleContentWindows;
import io.p13i.ra.models.SingleContentWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


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
        // First, process the content and window it
        List<SingleContentWindow> windows = getWindowedContent(content).stream()
                // Stem each window
                .map(WordVector::getStemmedSingleContentWindow)
                // Remove common words from each window
                .map(WordVector::removeMostCommonWords)
                .collect(Collectors.toList());
        return new MultipleContentWindows(windows);
    }

    /**
     * Breaks up a string into windows for improved indexing
     *
     * @param content the given string
     * @return a list of words
     */
    private static MultipleContentWindows getWindowedContent(String content) {
        // https://stackoverflow.com/a/454913/5071723
        String[] windowsAsStrings = content.split("\\r?\\n");

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
     * Stems a single window of content
     *
     * @param contentWindow the raw content window
     * @return a stemmed content window
     */
    private static SingleContentWindow getStemmedSingleContentWindow(SingleContentWindow contentWindow) {
        SingleContentWindow singleContentWindow = new SingleContentWindow(contentWindow.getWordVector().stream()
                .map(String::toLowerCase)
                .map(Stemmer::stem)
                .map(String::toUpperCase)
                .collect(Collectors.toList()));
        return singleContentWindow;
    }

    /**
     * Removes common words from {@code MOST_COMMON_WORDS}
     *
     * @param contentWindow the full vector in a window
     * @return a cleaner vector
     */
    private static SingleContentWindow removeMostCommonWords(SingleContentWindow contentWindow) {
        return new SingleContentWindow(contentWindow.stream()
                .filter(word -> !MOST_COMMON_WORDS.contains(word))
                .collect(Collectors.toList()));
    }
}
