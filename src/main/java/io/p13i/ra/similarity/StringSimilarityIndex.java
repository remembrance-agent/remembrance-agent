package io.p13i.ra.similarity;


import io.p13i.ra.utils.Assert;
import io.p13i.ra.cache.ICache;
import io.p13i.ra.cache.LimitedCapacityCache;
import io.p13i.ra.utils.Tuple;

/**
 * Compares strings and produces an index
 */
public class StringSimilarityIndex {
    /**
     * Caches queries to the calculate method
     */
    private static ICache<Tuple<String, String>, Double> mSimilarityCache = new LimitedCapacityCache<>(/* maxSize: */ 512);

    /**
     * Calculates the similarity between two strings using edit distance
     * @param x the first string
     * @param y the second string
     * @return a similarity index between 0.0 and 1.0
     */
    public static double calculate(String x, String y) {
        if (x == null || y == null) {
            return 0.0;
        }

        Tuple<String, String> params = Tuple.of(x, y);
        if (mSimilarityCache.hasKey(params)) {
            return mSimilarityCache.get(params);
        }

        int distance = levenshteinDistance(x, y);
        int longerStringLength = Math.max(x.length(), y.length());
        double index = (longerStringLength - distance) / (double) longerStringLength;

        Assert.inRange(index, 0.0, 1.0);

        mSimilarityCache.put(params, index);

        return index;
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
