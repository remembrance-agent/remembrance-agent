package io.p13i.ra.similarity;

import io.p13i.ra.utils.DateUtils;

import java.util.Date;

public class DateSimilarityIndex implements SimilarityIndex<Date> {
    public double calculate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return this.NO_SIMILARITY;
        }

        StringSimilarityIndex stringSimilarityIndex = new StringSimilarityIndex();
        double yearSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.YEAR_FORMAT), DateUtils.formatDate(d2, DateUtils.YEAR_FORMAT));
        double monthSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.MONTH_FORMAT), DateUtils.formatDate(d2, DateUtils.MONTH_FORMAT));
        double dayOfWeekSimilarity = stringSimilarityIndex.calculate(DateUtils.formatDate(d1, DateUtils.DAY_OF_WEEK_FORMAT), DateUtils.formatDate(d2, DateUtils.DAY_OF_WEEK_FORMAT));

        return (yearSimilarity + monthSimilarity + dayOfWeekSimilarity) / 3.0;
    }
}
