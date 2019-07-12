package io.p13i.ra.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class FileIO {
    private static final Logger LOGGER = LoggerUtils.getLogger(FileIO.class);

    /**
     * Reads all file contents as a string
     * @param filePath absolute file path
     * @return entire file as a string
     */
    public static String read(String filePath) {
        LOGGER.info("READ: " + filePath);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),"utf-8"));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static List<String> ls(String folderPath) {
        List<String> items = new ArrayList<>();
        File[] files = new File(folderPath).listFiles();
        for (File file : files) {
            items.add(file.getAbsolutePath());
        }
        return items;
    }

    /**
     * Lists documents in a directory
     * @param folderPath absolute folder path
     * @return list of absolute paths of folders/file in this directory
     */
    public static List<String> listFiles(String folderPath) {
        List<String> filePaths = new ArrayList<>();
        List<String> folderFilePaths = ls(folderPath);
        for (String filePath : folderFilePaths) {
            if (FileIO.isFile(filePath)) {
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    public static List<String> listFolders(String folderPath) {
        List<String> filePaths = new ArrayList<>();
        List<String> folderFilePaths = ls(folderPath);
        for (String filePath : folderFilePaths) {
            if (FileIO.isFolder(filePath)) {
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    public static List<String> listFolderFilesRecursive(String folderPath) {
        List<String> allFilesRecursively = new ArrayList<>();

        List<String> rootSubItems = ls(folderPath);
        for (String subItem : rootSubItems) {
            if (isFile(subItem)) {
                allFilesRecursively.add(subItem);
            } else if (isFolder(subItem)) {
                allFilesRecursively.addAll(listFolderFilesRecursive(subItem));
            }
        }

        return allFilesRecursively;
    }

    public static boolean isFolder(String absolutePath) {
        return new File(absolutePath).isDirectory();
    }

    public static boolean isFile(String absolutePath) {
        return new File(absolutePath).isFile();
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
