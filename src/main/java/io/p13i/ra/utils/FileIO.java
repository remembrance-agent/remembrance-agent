package io.p13i.ra.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileIO {
    /**
     * Reads all file contents as a string
     * @param filePath absolute file path
     * @return entire file as a string
     */
    public static String read(String filePath) {
        try {
            return Files.readString(new File(filePath).toPath());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Lists documents in a directory
     * @param folderPath absolute folder path
     * @return list of absolute paths of folders/file in this directory
     */
    public static List<String> listDirectory(String folderPath) {
        List<String> filePaths = new ArrayList<>();
        File folder = new File(folderPath);
        File[] folderFiles = folder.listFiles();
        for (File file : folderFiles) {
            if (file.isFile()) {
                filePaths.add(file.getAbsolutePath());
            }
        }
        return filePaths;
    }

    /**
     * Gets the name of a file
     * @param fromAbsolutePath from the absolute path of the file
     * @return as a string
     */
    public static String getFileName(String fromAbsolutePath) {
        return new File(fromAbsolutePath).getName();
    }

    /**
     * Gets the last modified date
     * @param absoluteFilePath of the given file at this path
     * @return as a Date object
     */
    public static Date getLastModifiedDate(String absoluteFilePath) {
        return new Date(new File(absoluteFilePath).lastModified() * 1000L);
    }
}
