package io.p13i.ra.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;


/**
 * Wrapper class for interactions with the system shell
 */
public class CommandLine {
    public static ArrayList<String> execute(final String command) {
        return execute(command, null);
    }

    /**
     * @return null if it failed for some reason.
     * @author https://stackoverflow.com/a/27437171/5071723
     */
    public static ArrayList<String> execute(final String command, final String directory) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command)
                    .redirectErrorStream(true);

            if (directory != null) {
                    processBuilder.directory(new File(directory));
            }

            Process process = processBuilder.start();

            ArrayList<String> output = new ArrayList<>();

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = br.readLine()) != null) {
                output.add(line);
            }

            // There should really be a timeout here.
            if (process.waitFor() != 0) {
                return null;
            }

            return output;

        } catch (Exception e) {
            //Warning: doing this is no good in high quality applications.
            //Instead, present appropriate error messages to the user.
            //But it's perfectly fine for prototyping.
            throw new RuntimeException(e);
        }
    }
}
