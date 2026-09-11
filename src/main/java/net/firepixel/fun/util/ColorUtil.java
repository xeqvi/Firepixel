package net.firepixel.fun.util;

import org.bukkit.ChatColor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&#([A-F0-9]{6})");

    private ColorUtil() {
    }

    public static String color(String text) {
        if (text == null) {
            return "";
        }

        return ChatColor.translateAlternateColorCodes('&', translateHex(text));
    }

    public static List<String> color(List<String> lines) {
        ArrayList<String> colored = new ArrayList<String>();

        if (lines == null) {
            return colored;
        }

        for (String line : lines) {
            colored.add(color(line));
        }

        return colored;
    }

    private static String translateHex(String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(resolveHex(hex)));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String resolveHex(String hex) {
        if (hex == null || hex.trim().isEmpty()) {
            return "";
        }

        try {
            Class<?> chatColorClass = Class.forName("net.md_5.bungee.api.ChatColor");
            Method of = chatColorClass.getMethod("of", String.class);
            Object value = of.invoke(null, "#" + hex);
            return value == null ? "" : value.toString();
        } catch (Throwable ignored) {
            return "";
        }
    }

    public static String replace(String text, Map<String, String> placeholders) {
        String result = text == null ? "" : text;

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }

        return result;
    }
}