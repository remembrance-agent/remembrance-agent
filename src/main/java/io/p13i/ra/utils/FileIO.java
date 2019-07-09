package io.p13i.ra.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileIO {
    public static String read(String filePath) {
        try {
            List<String> allLines = Files.readAllLines(new File(filePath).toPath());
            return String.join("", allLines);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
}
