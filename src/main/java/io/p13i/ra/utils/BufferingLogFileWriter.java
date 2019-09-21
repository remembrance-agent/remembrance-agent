package io.p13i.ra.utils;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;

/**
 * Writes log files to a after a specified number of items are writen to the buffer
 */
public class BufferingLogFileWriter implements Flushable, Closeable {

    public static final int MAX_QUEUE_SIZE = 50;
    public final LimitedCapacityBuffer<String> mBuffer = new LimitedCapacityBuffer<>(MAX_QUEUE_SIZE);
    private String logFilePath;
    private FileIOWriter writer;

    public BufferingLogFileWriter(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    /**
     * Opens the print writer
     */
    public synchronized void open() {
        try {
            this.writer = new FileIOWriter(this.logFilePath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Queues a file. If the capacity is exceed, flushes the buffer
     *
     * @param line the line to add to the queue
     */
    public synchronized void queue(String line) {
        if (mBuffer.size() >= MAX_QUEUE_SIZE) {
            this.flush();
        }

        mBuffer.add(line);
    }

    /**
     * Empties the queue into the print writer
     */
    public synchronized void flush() {
        while (!mBuffer.isEmpty()) {
            String line = mBuffer.removeLast();
            this.writer.write(line);
            if (!line.endsWith("\n")) {
                this.writer.write("\n");
            }
        }
    }

    @Override
    public synchronized void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
