package io.p13i.ra.utils;

public class GoogleChrome {
    public static String getURLofActiveTab() {
        return OS.appleScript("tell application \"Google Chrome\" to return URL of active tab of front window");
    }

    public static String getTitleOfActiveTab() {
        return OS.appleScript("tell application \"Google Chrome\" to return title of active tab of front window");
    }
}
