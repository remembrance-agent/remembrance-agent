package io.p13i.ra.similarity;

import io.p13i.ra.utils.Assert;
import io.p13i.ra.utils.DateUtils;

import java.util.Date;

/**
 * Computes an index [0, 1] between two Date objects
 */
public class DateSimilarityIndex {
    public static double calculate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return 0.0;
        }

        double yearSimilarity = StringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.YEAR_FORMAT), DateUtils.formatDate(d2, DateUtils.YEAR_FORMAT));
        double monthSimilarity = StringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.MONTH_FORMAT), DateUtils.formatDate(d2, DateUtils.MONTH_FORMAT));
        double dayOfWeekSimilarity = StringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.DAY_OF_WEEK_FORMAT), DateUtils.formatDate(d2, DateUtils.DAY_OF_WEEK_FORMAT));

        double index = (yearSimilarity + monthSimilarity + dayOfWeekSimilarity) / 3.0;

        Assert.inRange(index, 0.0, 1.0);

        return index;
    }
}
