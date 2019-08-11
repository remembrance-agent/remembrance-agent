package io.p13i.ra.similarity;

import io.p13i.ra.utils.Assert;

public class StringSimilarityIndex implements ISimilarityIndex<String> {
    public double calculate(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return this.NO_SIMILARITY;
        }
        int distance = levenshteinDistance(s1, s2);
        int longerStringLength = Math.max(s1.length(), s2.length());
        double index = (longerStringLength - distance) / (double) longerStringLength;

        return checkInBounds(index);
    }

    /**
     * https://www.baeldung.com/java-levenshtein-distance
     * O(m * n)
     */
    private static int levenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];

        for (int i = 0; i <= x.length(); i++) {
            for (int j = 0; j <= y.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                }
                else if (j == 0) {
                    dp[i][j] = i;
                }
                else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + costOfSubstitution(x.charAt(i - 1), y.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[x.length()][y.length()];
    }

    /**
     * https://www.baeldung.com/java-levenshtein-distance
     */
    private static int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    /**
     * https://www.baeldung.com/java-levenshtein-distance
     */
    private static int min(int... numbers) {
        int minValue = Integer.MAX_VALUE;
        for (int number : numbers) {
            if (number < minValue) {
                minValue = number;
            }
        }
        return minValue;
    }
}
