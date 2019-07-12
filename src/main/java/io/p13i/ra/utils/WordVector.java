package io.p13i.ra.utils;

import java.util.*;


/**
 * Wraps computations around converting strings to word vectors
 */
public class WordVector {
    // https://en.wikipedia.org/wiki/Most_common_words_in_English#100_most_common_words
    private static final String[] MOST_COMMON_WORDS_100_WIKIPEDIA_OEC = {
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
    };

    public static List<String> getWordVector(String content) {
        List<String> wordVector = ListUtils.fromArray(content
                .toLowerCase()
                // Remove non (alphanumeric, :, space) characters
                .replaceAll("[^a-zA-Z\\d\\s:]", "")
                // Split on colon or space
                .split("[ |:]"), new ListUtils.Filter<String>() {
            @Override
            public boolean shouldInclude(String item) {
                return item != null && item.length() > 0;
            }
        });

        if (wordVector.size() == 1 && wordVector.get(0).length() == 0) {
            wordVector.remove(0);
        }

        return wordVector;
    }

    public static List<String> removeMostCommonWords(List<String> allWords) {
        List<String> cleanedWords = new ArrayList<>(allWords.size());
        Set<String> commonWords = new HashSet<>(Arrays.asList(MOST_COMMON_WORDS_100_WIKIPEDIA_OEC));
        for (String word : allWords) {
            if (!commonWords.contains(word)) {
                cleanedWords.add(word);
            }
        }
        return cleanedWords;
    }
}
