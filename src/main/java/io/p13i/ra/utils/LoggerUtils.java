package io.p13i.ra.utils;

import io.p13i.ra.RemembranceAgentClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.*;

public class LoggerUtils {
    public static <T> Logger getLogger(Class<T> forClass) {
        Logger LOGGER = Logger.getLogger(forClass.getName());

        LOGGER.addHandler(new ConsoleHandler());
        LOGGER.addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                String message = null;
                if (!isLoggable(record))
                    return;
                Formatter formatter = getFormatter();
                if (formatter != null) {
                    message = formatter.format(record);
                } else {
                    message = record.getMessage();
                }
                RemembranceAgentClient.sTextArea.append(DateUtils.timestamp());
                RemembranceAgentClient.sTextArea.append(" | ");
                RemembranceAgentClient.sTextArea.append(message);
                RemembranceAgentClient.sTextArea.append("\n");
            }

            @Override
            public void flush() {

            }

            @Override
            public void close() throws SecurityException {

            }
        });
        return LOGGER;
    }
}
