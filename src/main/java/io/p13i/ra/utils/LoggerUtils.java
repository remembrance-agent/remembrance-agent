package io.p13i.ra.utils;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

/**
 * utilities for working with the logger
 */
public class LoggerUtils {

    /**
     * Gets a logger for the given class or throws an exception
     *
     * @param forClass the class in questino
     * @param <T>      the type of class
     * @return a Logger instance or an exception
     * @throws RuntimeException if a logger couldn't be created
     */
    public static <T> Logger getLogger(Class<T> forClass) throws RuntimeException {
        try {
            return getLoggerInternal(forClass);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static <T> Logger getLoggerInternal(Class<T> forClass) throws IOException {
        Logger LOGGER = Logger.getLogger(forClass.getName());
        LOGGER.setLevel(Level.INFO);
        LOGGER.addHandler(new ConsoleHandler() {{
            setFormatter(new LoggerUtils.DefaultTimestampedFormatter());
        }});
        return LOGGER;
    }

    static class DefaultTimestampedFormatter extends SimpleFormatter {
        private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format, new Date(lr.getMillis()), lr.getLevel().getLocalizedName(), lr.getMessage());
        }
    }
}
