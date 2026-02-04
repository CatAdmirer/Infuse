package com.catadmirer.infuseSMP.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtil {
    public static Component formatTime(long totalSeconds, TextColor color) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        String timeString = minutes + ":" + String.format("%02d", seconds);
        return Component.text(timeString).color(color).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
    }
}