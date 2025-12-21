package com.catadmirer.infuseSMP.util;

import java.util.regex.Pattern;

public class MessageUtil {
    public static String stripAllColors(String input) {
        if (input == null) return null;

        Pattern pattern = Pattern.compile(
                "(§#[0-9a-fA-F]{6})" + "|(§x(§[0-9a-fA-F]){6})" + "|(§[0-9a-fk-orA-FK-OR])" + "|(�x(�[0-9a-fA-F]){6})" + "�");

        return pattern.matcher(input).replaceAll("");
    }
}
