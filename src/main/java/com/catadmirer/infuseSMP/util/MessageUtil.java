package com.catadmirer.infuseSMP.util;

import net.kyori.adventure.text.format.TextColor;

public class MessageUtil {
    public static String formatTime(long totalSeconds, TextColor color) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return color + "<b>" + timeString + "<reset>";
    }
}