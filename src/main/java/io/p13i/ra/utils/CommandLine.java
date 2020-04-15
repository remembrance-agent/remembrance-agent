package io.p13i.ra.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Wrapper class for interactions with the system shell
 */
public class CommandLine {
    public static Logger LOGGER = LoggerUtils.getLogger(CommandLine.class);

    public static class Result {
        public List<String> lines;
        public int exitCode;

        public Result(List<String> lines, int exitCode) {
            this.lines = lines;
            this.exitCode = exitCode;
        }

        public String getLinesAsString() {
            StringBuilder stringBuilder = new StringBuilder();
            for (String line : lines) {
                stringBuilder.append(line);
            }
            return stringBuilder.toString();
        }
    }

    public static Result execute(final String command) {
        return execute(command, null);
    }

    /**
     * @return null if it failed for some reason.
     * @author https://stackoverflow.com/a/27437171/5071723
     */
    public static Result execute(final String command, final String directory) {
        if (command.contains("\"")) {
            LOGGER.warning("String command includes \" character. May lead to unexpected results as bash -c is used to evaluate commands.");
        }

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

            int exitCode = process.waitFor();

            return new Result(output, exitCode);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
