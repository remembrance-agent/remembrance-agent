package io.p13i.ra.utils;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileIOWriter implements Closeable {

    private final FileWriter fileWriter;
    private final BufferedWriter bufferedWriter;
    private final PrintWriter printWriter;

    public FileIOWriter(String filePath) throws IOException {
        fileWriter = new FileWriter(filePath, true);
        bufferedWriter = new BufferedWriter(fileWriter);
        printWriter = new PrintWriter(bufferedWriter);
    }

    @Override
    public void close() throws IOException {
        printWriter.close();
        bufferedWriter.close();
        fileWriter.close();
    }

    public void write(String text) {
        printWriter.print(text);
        printWriter.flush();
    }
}
