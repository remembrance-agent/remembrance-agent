package io.p13i.ra.utils;

import io.p13i.ra.cache.Cache;
import io.p13i.ra.cache.AbstractCache;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Callable;

/**
 * Utilities for formatting dates
 */
public class DateUtils {

    public static final String YEAR_FORMAT = "yyyy";
    public static final String MONTH_FORMAT = "MM";
    public static final String DAY_FORMAT = "dd";
    public static final String DAY_OF_WEEK_FORMAT = "EEE";
    public static final String HOUR_FORMAT = "HH";
    public static final String MINUTE_FORMAT = "mm";
    public static final String SECOND_FORMAT = "ss";

    private static final String TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * Use a normal cache because the number of elements is limited to under 10 (upon code inspection).
     */
    private static AbstractCache<String, DateFormat> sDateFormatCache = new Cache<>();

    /**
     * Formats a date
     *
     * @param date    the date to format
     * @param pattern the pattern to use
     * @return the formatted date
     * @throws io.p13i.ra.utils.Arguments.NullArgumentException if date or pattern is null
     */
    public static String formatDate(Date date, final String pattern) {
        Arguments.Ensure.NotNull(date, pattern);

        return sDateFormatCache.get(pattern, new Callable<DateFormat>() {
            @Override
            public DateFormat call() throws Exception {
                return new SimpleDateFormat(pattern);
            }
        }).format(date);
    }

    /**
     * Gets the current time
     *
     * @return the current time as a date
     */
    public static Date now() {
        return Calendar.getInstance().getTime();
    }

    /**
     * Gets now() as a timestamp
     *
     * @return a long timestamp
     */
    public static long longTimestamp() {
        return now().getTime();
    }

    /**
     * Gets the timestamp of the given date
     *
     * @param date the date
     * @return a formatted string timestamp
     */
    public static String timestampOf(Date date) {
        if (date == null) {
            return null;
        }

        return formatDate(date, TIMESTAMP_FORMAT);
    }

    /**
     * Computes the difference (d2 - d1) in seconds between two dates
     *
     * @param d1 the first date
     * @param d2 the second date
     * @return the difference in seconds
     */
    public static long deltaSeconds(Date d1, Date d2) {
        Arguments.Ensure.NotNull(d1, d2);
        long difference = d2.getTime() - d1.getTime();
        return difference / 1000;
    }

    /**
     * Parses the given timestamp string based on the TIMESTAMP_FORMAT
     *
     * @param timestamp the timestamp string
     * @return a Date or null if parsing failed or if the provided timestamp is null or whitespace
     */
    public static Date parseTimestamp(String timestamp) {
        if (StringUtils.isNullOrWhitespace(timestamp) || timestamp.equals("null")) {
            return null;
        }

        try {
            return sDateFormatCache.get(TIMESTAMP_FORMAT, new Callable<DateFormat>() {
                @Override
                public DateFormat call() throws Exception {
                    return new SimpleDateFormat(TIMESTAMP_FORMAT);
                }
            }).parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
