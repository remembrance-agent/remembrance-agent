package io.p13i.ra.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandLineUtils {
    public static String execute(String[] command) {
        try {
            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = stdInput.readLine()) != null) {
                sb.append(s);
            }

            return sb.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
