package io.p13i.ra.similarity;

import io.p13i.ra.utils.Assert;
import io.p13i.ra.utils.DateUtils;

import java.util.Date;

/**
 * Computes an index [0, 1] between two Date objects
 */
public class DateSimilarityIndex {
    /**
     * Calculates the similarity between two dates
     * @param d1 the first date
     * @param d2 the second date
     * @return a similarity index between 0.0 and 1.0
     */
    public static double calculate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return 0.0;
        }

        long nowTime = DateUtils.now().getTime();

        long newTime = Math.max(d1.getTime(), d2.getTime());
        long olderTime = Math.min(d1.getTime(), d2.getTime());

        double index = 1 - ((newTime - olderTime) / (double) nowTime);

        Assert.inRange(index, 0.0, 1.0);

        return index;
    }
}
