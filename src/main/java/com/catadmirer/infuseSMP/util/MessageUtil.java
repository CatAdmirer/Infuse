package com.catadmirer.infuseSMP.util;

public class MessageUtil {
    public static String formatTime(long totalSeconds, String color) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return String.format("%s<b>%s", color, timeString);
    }
}