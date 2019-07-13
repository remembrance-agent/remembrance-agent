package io.p13i.ra.utils;

import io.p13i.ra.RemembranceAgentClient;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggerUtils {

    private static FileHandler sDefaultFileHandler;

    public static FileHandler getDefaultFileHandler() {
        if (sDefaultFileHandler == null) {
            try {
                sDefaultFileHandler = new FileHandler(RemembranceAgentClient.sRAClientLogFilePath) {{
                    setFormatter(new DefaultTimestampedFormatter());
                }};
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sDefaultFileHandler;
    }

    public static <T> Logger getLogger(Class<T> forClass) {
        Logger LOGGER = Logger.getLogger(forClass.getName());
        LOGGER.setLevel(Level.INFO);
        LOGGER.addHandler(new ConsoleHandler() {{
            setFormatter(new LoggerUtils.DefaultTimestampedFormatter());
        }});
        LOGGER.addHandler(getDefaultFileHandler());
        return LOGGER;
    }

    static class DefaultTimestampedFormatter extends SimpleFormatter {
        private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format,
                    new Date(lr.getMillis()),
                    lr.getLevel().getLocalizedName(),
                    lr.getMessage()
            );
        }
    }
}
