package io.p13i.ra.similarity;

import io.p13i.ra.utils.Assert;
import io.p13i.ra.utils.DateUtils;

import java.util.Date;

/**
 * Computes an index [0, 1] between two Date objects
 */
public class DateSimilarityIndex implements SimilarityIndex<Date> {
    public double calculate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return this.NO_SIMILARITY;
        }

        StringSimilarityIndex stringSimilarityIndex = new StringSimilarityIndex();
        double yearSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.YEAR_FORMAT), DateUtils.formatDate(d2, DateUtils.YEAR_FORMAT));
        double monthSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.MONTH_FORMAT), DateUtils.formatDate(d2, DateUtils.MONTH_FORMAT));
        double dayOfWeekSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.DAY_OF_WEEK_FORMAT), DateUtils.formatDate(d2, DateUtils.DAY_OF_WEEK_FORMAT));

        double index = (yearSimilarity + monthSimilarity + dayOfWeekSimilarity) / 3.0;

        Assert.inRange(index, INDEX_LOWER_BOUND, INDEX_HIGHER_BOUND);

        return index;
    }
}
