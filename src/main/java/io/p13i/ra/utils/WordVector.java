package io.p13i.ra.utils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Wraps computations around converting strings to word vectors
 */
public class WordVector {
    private static final int MINIMUM_WORD_LENGTH = 2;

    // Based on https://en.wikipedia.org/wiki/Most_common_words_in_English#100_most_common_words
    private static final Set<String> MOST_COMMON_WORDS = new HashSet<>(Arrays.asList("is", "the", "be", "to", "of", "and", "a", "in", "that", "have", "i", "it", "for", "not", "on", "with", "he", "as", "you", "do", "at", "this", "but", "his", "by", "from", "they", "we", "say", "her", "she", "or", "an", "will", "my", "one", "all", "would", "there", "their", "what", "so", "up", "out", "if", "about", "who", "get", "which", "go", "me", "when", "make", "can", "like", "time", "no", "just", "him", "know", "take", "people", "into", "year", "your", "good", "some", "could", "them", "see", "other", "than", "then", "now", "look", "only", "come", "its", "over", "think", "also", "back", "after", "use", "two", "how", "our", "work", "first", "well", "way", "even", "new", "want", "because", "any", "these", "give", "day", "most", "us"));

    /**
     * Tokenizes a string
     * @param content the given string
     * @return a list of words
     */
    public static List<String> getWordVector(String content) {

        List<String> wordVector = new ArrayList<>();
        boolean wordMode = true;

        StringBuilder stringBuilder = new StringBuilder();
        for (Character c : content.toCharArray()) {
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
     * Removes common words from {@code MOST_COMMON_WORDS}
     * @param allWords the full vector
     * @return a cleaner vector
     */
    public static List<String> removeMostCommonWords(List<String> allWords) {
        return allWords.stream()
                .filter(word -> !MOST_COMMON_WORDS.contains(word))
                .collect(Collectors.toList());
    }
}
