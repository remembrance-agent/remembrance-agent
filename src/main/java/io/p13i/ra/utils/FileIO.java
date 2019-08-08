package io.p13i.ra.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8));

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static PrintWriter openPrintWriter(String filePath) {
        try {
            return new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void write(String filePath, String text) {
        LOGGER.info(String.format("Writing ~%d bytes to %s", text.length(), filePath));
        if (!FileIO.fileExists(filePath)) {
            FileIO.createFile(filePath);
        }

        PrintWriter out = openPrintWriter(filePath);
        out.print(text);
        out.close();
    }

    private static void createFile(String filePath) {
        try {
            new File(filePath).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean fileExists(String filePath) {
        return new File(filePath).isFile();
    }

    public static void write(String filePath, Character c) {
        write(filePath, c.toString());
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

    private static boolean isFolder(String absolutePath) {
        return new File(absolutePath).isDirectory();
    }

    private static boolean isFile(String absolutePath) {
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

    public static String getEnclosingFolderName(String absolutePath) {
        return new File(absolutePath).getParentFile().getName();
    }

    /**
     * Gets the last modified date
     * @param absoluteFilePath of the given file at this path
     * @return as a Date object
     */
    public static Date getLastModifiedDate(String absoluteFilePath) {
        return new Date(new File(absoluteFilePath).lastModified() * 1000L);
    }

    /**
     * Remove non-alpha numeric characters from the string
     * @param dirtyName A string with all sorts of characters
     * @return with only alpha numeric characters
     */
    public static String getCleanName(String dirtyName) {
        return dirtyName.replaceAll("[^a-zA-Z0-9]", "");
    }

    public static void delete(String fileName) {
        new File(fileName).delete();
    }
}
