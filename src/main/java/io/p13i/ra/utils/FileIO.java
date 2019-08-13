package io.p13i.ra.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Easy-to-use rapper for file input/output operations
 */
public class FileIO {

    /**
     * Reads all file contents as a string
     *
     * @param filePath absolute file path
     * @return entire file as a string
     */
    public static String read(String filePath) {
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

    /**
     * Gets a {@code PrintWriter} for the specified file path
     *
     * @param filePath the file's path
     * @return opens a {@code PrintWriter}
     */
    public static PrintWriter openPrintWriter(String filePath) {
        try {
            return new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Writes the given text to the file at the specified path
     *
     * @param filePath the file's path
     * @param text     the text to write
     */
    public static void write(String filePath, String text) {
        if (!FileIO.fileExists(filePath)) {
            FileIO.createFile(filePath);
        }

        try (PrintWriter out = openPrintWriter(filePath)) {
            Objects.requireNonNull(out).print(text);
        }
    }

    /**
     * Creates a file
     *
     * @param filePath the file's path
     * @return whether or not the file was created
     */
    private static boolean createFile(String filePath) {
        try {
            return new File(filePath).createNewFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks whether a file exists
     *
     * @param filePath the file's path
     * @return whether or not the file exists
     */
    private static boolean fileExists(String filePath) {
        return new File(filePath).isFile();
    }

    /**
     * Lists a directory's contents
     *
     * @param folderPath
     * @return
     */
    private static List<String> ls(String folderPath) {
        File[] files = new File(folderPath).listFiles();

        if (files == null) {
            return Collections.emptyList();
        }

        return Arrays.stream(files)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    /**
     * Lists files in a directory
     *
     * @param folderPath absolute folder path
     * @return list of absolute paths of folders/file in this directory
     */
    public static List<String> listFiles(String folderPath) {
        return ls(folderPath).stream()
                .filter(FileIO::isFile)
                .collect(Collectors.toList());
    }

    /**
     * Lists the files in a folder, recursively
     *
     * @param folderPath the path of the folder
     * @return all the files in a folder
     */
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

    /**
     * Checks whether a path is a folder
     *
     * @param absolutePath the path
     * @return whether or not the path is a folder
     */
    private static boolean isFolder(String absolutePath) {
        return new File(absolutePath).isDirectory();
    }

    /**
     * Checks whether a path is a file
     *
     * @param absolutePath the path
     * @return whether or not the path is a file
     */
    private static boolean isFile(String absolutePath) {
        return new File(absolutePath).isFile();
    }

    /**
     * Gets the name of a file
     *
     * @param fromAbsolutePath from the absolute path of the file
     * @return as a string
     */
    public static String getFileName(String fromAbsolutePath) {
        return new File(fromAbsolutePath).getName();
    }

    /**
     * Gets the base (enclosing folder) name of a file or folder
     *
     * @param absolutePath the path of the file or folder
     * @return the enclosing folder's name
     */
    public static String getEnclosingFolderName(String absolutePath) {
        return new File(absolutePath).getParentFile().getName();
    }

    /**
     * Gets the last modified date
     *
     * @param absoluteFilePath of the given file at this path
     * @return as a Date object
     */
    public static Date getLastModifiedDate(String absoluteFilePath) {
        return new Date(new File(absoluteFilePath).lastModified() * 1000L);
    }

    /**
     * Remove non-alpha numeric characters from the string
     *
     * @param dirtyName A string with all sorts of characters
     * @return with only alpha numeric characters
     */
    public static String getCleanName(String dirtyName) {
        return dirtyName.replaceAll("[^a-zA-Z0-9]", "");
    }

    /**
     * Deletes a file
     *
     * @param fileName the absolute path
     * @return whether or not the file was deleted
     */
    public static boolean delete(String fileName) {
        return new File(fileName).delete();
    }
}
