package com.catadmirer.infuseSMP.util;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtil {
    public static String stripAllColors(String input) {
        if (input == null) return null;

        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" + "|(§x(§[0-9a-fA-F]){6})" + "|(§[0-9a-fk-orA-FK-OR])" + "|(�x(�[0-9a-fA-F]){6})" + "�");

        return pattern.matcher(input).replaceAll("");
    }

    public static String applyHexColors(String input) {
        String regex = "(#(?:[0-9a-fA-F]{6}))";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String hexCode = matcher.group(1);
            String colorCode = ChatColor.of(hexCode).toString();
            matcher.appendReplacement(result, colorCode);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public static List<String> applyLore(List<String> input) {
        String regex = "#[0-9a-fA-F]{6}";
        Pattern pattern = Pattern.compile(regex);

        List<String> output = new ArrayList<>();

        for (String line : input) {
            Matcher matcher = pattern.matcher(line);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String hex = matcher.group();
                String color = ChatColor.of(hex).toString();
                matcher.appendReplacement(result, color);
            }
            matcher.appendTail(result);

            output.add(result.toString());
        }

        return output;
    }

}
