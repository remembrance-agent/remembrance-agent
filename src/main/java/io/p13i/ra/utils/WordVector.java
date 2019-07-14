package io.p13i.ra.utils;

import java.util.*;


/**
 * Wraps computations around converting strings to word vectors
 */
public class WordVector {
    private static final int MINIMUM_WORD_LENGTH = 2;

    // https://en.wikipedia.org/wiki/Most_common_words_in_English#100_most_common_words
    private static final String[] MOST_COMMON_WORDS_100_WIKIPEDIA_OEC = {
            "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"
    };

    public static List<String> getWordVector(String content) {

        List<String> wordVector = new ArrayList<>();
        boolean wordMode = true;

        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : content.toCharArray()) {
            if (CharacterUtils.isAlphanumeric(c)) {
                wordMode = true;
                stringBuilder.append(c.toString().toLowerCase());
            } else {
                wordMode = false;
                if (stringBuilder.length() >= MINIMUM_WORD_LENGTH) {
                    wordVector.add(stringBuilder.toString());
                    stringBuilder = new StringBuilder();
                }
            }
        }

        if (wordMode && stringBuilder.length() >= MINIMUM_WORD_LENGTH) {
            wordVector.add(stringBuilder.toString());
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
