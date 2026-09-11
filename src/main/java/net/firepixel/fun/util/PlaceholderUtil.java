package net.firepixel.fun.util;

import net.firepixel.fun.Firepixel;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PlaceholderUtil {

    private static final Pattern SERVER_TIME_PATTERN = Pattern.compile("%server_time_([^%]+)%");

    private PlaceholderUtil() {
    }

    public static String resolve(Player player, String text) {
        if (text == null) {
            return "";
        }

        String result = text;
        result = applyServerPlaceholders(result);
        result = applyPlayerPlaceholders(result, player);
        result = applyRankPlaceholders(result, player);
        result = applyPlaceholderApi(player, result);
        return result;
    }

    private static String applyServerPlaceholders(String text) {
        String result = text;
        String time = new SimpleDateFormat("HH:mm", Locale.US).format(new Date());
        result = result.replace("%server_time%", time);
        result = result.replace("{server_time}", time);
        result = result.replace("%server_id%", getServerId());
        result = result.replace("%server_name%", getServerName());
        result = result.replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        result = result.replace("%bungee_total%", String.valueOf(Bukkit.getOnlinePlayers().size()));
        result = replaceServerTimePatterns(result);
        return result;
    }

    private static String applyPlayerPlaceholders(String text, Player player) {
        if (player == null) {
            return text;
        }

        String result = text;
        result = result.replace("%player%", player.getName());
        result = result.replace("{player}", player.getName());
        result = result.replace("%player_name%", player.getName());
        result = result.replace("%uuid%", player.getUniqueId().toString());
        result = result.replace("%displayname%", player.getName());
        result = result.replace("%player_world%", player.getWorld().getName());
        return result;
    }

    private static String applyRankPlaceholders(String text, Player player) {
        if (player == null) {
            return text;
        }

        Firepixel plugin = Firepixel.getInstance();

        if (plugin == null || plugin.getRankManager() == null) {
            return text;
        }

        String result = text;
        Map<String, String> placeholders = plugin.getRankManager().getPlaceholderMap(player);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String value = entry.getValue() == null ? "" : entry.getValue();
            result = result.replace("%" + entry.getKey() + "%", value);
            result = result.replace("{" + entry.getKey() + "}", value);
        }

        String tag = plugin.getRankManager().getRankTag(player);
        result = result.replace("%rank%", tag);
        result = result.replace("{rank}", tag);
        result = result.replace("%prefix%", tag);
        result = result.replace("{prefix}", tag);
        result = result.replace("%luckperms_prefix%", tag);
        result = result.replace("%rank_name%", plugin.getRankManager().getRankName(player));
        result = result.replace("%rank_tag%", tag);
        result = result.replace("%rank_group%", plugin.getRankManager().resolveRankId(player));
        result = result.replace("%rank_display%", plugin.getRankManager().getDisplayName(player));
        return result;
    }

    private static String applyPlaceholderApi(Player player, String text) {
        if (player == null || text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }

        try {
            Class<?> placeholderApi = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = placeholderApi.getMethod("setPlaceholders", Player.class, String.class);
            Object value = method.invoke(null, player, text);

            if (value instanceof String) {
                return (String) value;
            }
        } catch (Throwable ignored) {
        }

        return text;
    }

    private static String replaceServerTimePatterns(String text) {
        if (text == null || text.indexOf("%server_time_") < 0) {
            return text == null ? "" : text;
        }

        Matcher matcher = SERVER_TIME_PATTERN.matcher(text);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String pattern = matcher.group(1);
            String replacement = "";

            try {
                replacement = new SimpleDateFormat(pattern, Locale.US).format(new Date());
            } catch (IllegalArgumentException ignored) {
            }

            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String getServerId() {
        Firepixel plugin = Firepixel.getInstance();

        if (plugin == null) {
            return "1";
        }

        Object configured = plugin.getConfig().get("server.id");

        if (configured == null) {
            return "1";
        }

        String value = String.valueOf(configured).trim();
        return value.isEmpty() ? "1" : value;
    }

    private static String getServerName() {
        Firepixel plugin = Firepixel.getInstance();

        if (plugin == null) {
            return "Lobby";
        }

        String configured = plugin.getConfig().getString("server.name", "Lobby");
        return configured == null || configured.trim().isEmpty() ? "Lobby" : configured;
    }

    public static String applyAll(Player player, String text) {
        return ColorUtil.color(resolve(player, text));
    }
}