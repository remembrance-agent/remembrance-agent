package io.p13i.ra.utils;

import io.p13i.ra.RemembranceAgentClient;

import java.io.IOException;
import java.util.Date;
import java.util.logging.*;

public class LoggerUtils {

    private static ConsoleHandler sDefaultConsoleHandler;
    private static FileHandler sDefaultFileHandler;

    private static FileHandler getDefaultFileHandlerInstance() {
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

    private static ConsoleHandler getDefaultConsoleHandlerInstance() {
        if (sDefaultConsoleHandler == null) {
            sDefaultConsoleHandler = new ConsoleHandler() {{
                setFormatter(new LoggerUtils.DefaultTimestampedFormatter());
            }};
        }
        return sDefaultConsoleHandler;
    }


    public static <T> Logger getLogger(Class<T> forClass) {
        Logger LOGGER = Logger.getLogger(forClass.getName());
        LOGGER.setLevel(Level.INFO);
        LOGGER.addHandler(getDefaultConsoleHandlerInstance());
        LOGGER.addHandler(getDefaultFileHandlerInstance());
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
