package io.p13i.ra.gui;

import io.p13i.ra.RemembranceAgentClient;

import java.io.File;
import java.util.Objects;

import static java.util.prefs.Preferences.userNodeForPackage;

public class User {

    public static String HOME = System.getProperty("user.home");

    public static class Preferences {

        private static String NOT_SET = "";

        public enum Pref {
            KeystrokesLogFile("KEYSTROKES_LOG_FILE_PATH_PREFS_NODE_NAME", HOME + File.separator + "keystrokes.log"),
            RAClientLogFile("RA_CLIENT_LOG_FILE_PATH_PREFS_NODE_NAME", HOME + File.separator + "ra.log"),
            LocalDiskDocumentsFolderPath("LOCAL_DISK_DOCUMENTS_FOLDER_PATH_PREFS_NODE_NAME", HOME),
            GoogleDriveFolderID("GOOGLE_DRIVE_FOLDER_ID_PREFS_NODE_NAME", NOT_SET),
            GmailMaxEmailsCount("GMAIL_MAX_EMAILS_COUNT_NODE_NAME", "10");

            private String nodeName;
            private String defaultValue;

            Pref(String nodeName, String defaultValue) {
                this.nodeName = nodeName;
                this.defaultValue = defaultValue;
            }
        }

        public static String get(Pref getPref) {

            for (Pref pref : Pref.values()) {
                if (getPref.nodeName.equals(pref.nodeName)) {
                    return userNodeForPackage(RemembranceAgentClient.class).get(getPref.nodeName, getPref.defaultValue);
                }
            }

            throw new IllegalArgumentException(getPref.toString());
        }

        public static void set(Pref pref, String value) {
            userNodeForPackage(RemembranceAgentClient.class).put(pref.nodeName, value);
        }
    }
}
