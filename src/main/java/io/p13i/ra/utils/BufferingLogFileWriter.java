package io.p13i.ra.utils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class BufferingLogFileWriter implements Flushable, Closeable {

    public static final Logger LOGGER = LoggerUtils.getLogger(BufferingLogFileWriter.class);

    public static final int MAX_QUEUE_SIZE = 50;
    public final Queue<String> mQueue = new LinkedList<>();
    private String logFilePath;
    private PrintWriter printWriter;

    public BufferingLogFileWriter(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public synchronized void open() {
        this.printWriter = FileIO.openPrintWriter(this.logFilePath);
    }

    public synchronized void queue(String line) {
        if (mQueue.size() > MAX_QUEUE_SIZE) {
            this.flush();
        }

        mQueue.add(line);
    }

    public synchronized void flush() {
        LOGGER.info("Flushing queue...");
        while (!mQueue.isEmpty()) {
            String line = mQueue.poll();
            this.printWriter.print(line);
            if (!line.endsWith("\n")) {
                this.printWriter.println();
            }
        }
        LOGGER.info("Flushed.");
    }

    public synchronized void close() {
        this.printWriter.close();
    }
}
