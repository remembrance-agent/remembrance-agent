package io.p13i.ra.utils;

import io.p13i.ra.gui.User;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

import static io.p13i.ra.gui.User.Preferences.Pref.RAClientLogFile;

public class LoggerUtils {

    public static <T> Logger getLogger(Class<T> forClass) {
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
        LOGGER.addHandler(new FileHandler(User.Preferences.getString(RAClientLogFile)) {{
            setFormatter(new DefaultTimestampedFormatter());
        }});
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
