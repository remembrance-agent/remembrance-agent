package io.p13i.ra.utils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Writes log files to a after a specified number of items are writen to the buffer
 */
public class BufferingLogFileWriter implements Flushable, Closeable {

    private static final Logger LOGGER = LoggerUtils.getLogger(BufferingLogFileWriter.class);

    public static final int MAX_QUEUE_SIZE = 50;
    public final Queue<String> mQueue = new LinkedList<>();
    private String logFilePath;
    private PrintWriter printWriter;

    public BufferingLogFileWriter(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Opens the print writer
     */
    public synchronized void open() {
        this.printWriter = FileIO.openPrintWriter(this.logFilePath);
    }

    /**
     * Queues a file. If the capacity is exceed, flushes the buffer
     * @param line the line to add to the queue
     */
    public synchronized void queue(String line) {
        if (mQueue.size() > MAX_QUEUE_SIZE) {
            this.flush();
        }

        mQueue.add(line);
    }

    /**
     * Empties the queue into the print writer
     */
    public synchronized void flush() {
        while (!mQueue.isEmpty()) {
            String line = mQueue.poll();
            this.printWriter.print(line);
            if (!line.endsWith("\n")) {
                this.printWriter.println();
            }
        }
    }

    @Override
    public synchronized void close() {
        this.printWriter.close();
    }
}
