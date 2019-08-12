package io.p13i.ra.similarity;


import io.p13i.ra.utils.*;

public class StringSimilarityIndex {
    public static double calculate(String x, String y) {
        if (x == null || y == null) {
            return 0.0;
        }

        Tuple<String, String> params = Tuple.of(x, y);
        if (similarityCache.hasKey(params)) {
            return similarityCache.get(params);
        }

        int distance = levenshteinDistance(x, y);
        int longerStringLength = Math.max(x.length(), y.length());
        double index = (longerStringLength - distance) / (double) longerStringLength;

        Assert.inRange(index, 0.0, 1.0);

        similarityCache.put(params, index);

        return index;
    }

    public static ICache<Tuple<String, String>, Double> similarityCache = new LimitedCapacityCache<>(512);

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
