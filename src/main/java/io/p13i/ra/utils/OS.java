package io.p13i.ra.utils;


import java.util.List;

public class OS {
    private static final String MAC_OS_X = "Mac OS X";

    public static String name() {
        return System.getProperty("os.name");
    }

    public static String appleScript(String script) {
        if (!name().equals(MAC_OS_X)) {
            throw new RuntimeException("OS " + name() + " doesn't support Apple Script");
        }

        List<String> result = CommandLine.execute("osascript -e '" + script + "'");

        StringBuilder stringBuilder = new StringBuilder();
        for (String line : result) {
            stringBuilder.append(line);
        }

        return stringBuilder.toString();
    }
}
