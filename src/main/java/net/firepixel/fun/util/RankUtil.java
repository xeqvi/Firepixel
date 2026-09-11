package net.firepixel.fun.util;

import org.bukkit.entity.Player;

import java.util.Locale;

public final class RankUtil {

    private RankUtil() {
    }

    public static String getGroup(Player player) {
        if (player == null) {
            return "default";
        }

        try {
            Class<?> providerClass = Class.forName("net.luckperms.api.LuckPermsProvider");
            Object luckPerms = providerClass.getMethod("get").invoke(null);

            if (luckPerms == null) {
                return "default";
            }

            Object userManager = luckPerms.getClass().getMethod("getUserManager").invoke(luckPerms);
            Object user = userManager.getClass().getMethod("getUser", java.util.UUID.class).invoke(userManager, player.getUniqueId());

            if (user == null) {
                return "default";
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object group = metaData.getClass().getMethod("getPrimaryGroup").invoke(metaData);
            return group == null ? "default" : group.toString().toLowerCase(Locale.ROOT);
        } catch (Throwable ignored) {
        }

        return "default";
    }

    public static String getTag(Player player) {
        return getTag(getGroup(player));
    }

    public static String getTag(String rawGroup) {
        String group = rawGroup == null ? "default" : rawGroup.toLowerCase(Locale.ROOT);

        if (group.equals("owner")) return "&c[OWNER]";
        if (group.equals("coowner")) return "&c[CO OWNER]";
        if (group.equals("founder")) return "&c[FOUNDER]";
        if (group.equals("mainadmin")) return "&c[MAIN ADMIN]";
        if (group.equals("admin")) return "&c[ADMIN]";
        if (group.equals("mod")) return "&2[MOD]";
        if (group.equals("gm")) return "&2[GM]";
        if (group.equals("helper")) return "&9[HELPER]";
        if (group.equals("jrhelper")) return "&9[JR HELPER]";
        if (group.equals("youtube")) return "&c[&fYOUTUBE&c]";
        if (group.equals("mvp++") || group.equals("mvpplusplus")) return "&b[MVP&f++&b]";
        if (group.equals("mvp+") || group.equals("mvpplus")) return "&b[MVP&f+&b]";
        if (group.equals("mvp")) return "&b[MVP]";
        if (group.equals("vip+") || group.equals("vipplus")) return "&a[VIP&6+&a]";
        if (group.equals("vip")) return "&a[VIP]";
        return "";
    }
}