package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PunishmentManager {

    public enum PunishmentType {
        BAN, TEMPBAN, MUTE, TEMPMUTE, WARN, KICK
    }

    private final Firepixel plugin;

    public PunishmentManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public String buildScreen(String language, String key, String reason, String duration) {
        String appeal = plugin.getConfig().getString("report.appeal-link", "https://firepixel.fun/appeal");
        String message = plugin.getLanguageManager().getMessage(language, "punishments.screens." + key);
        message = message.replace("%reason%", reason == null ? "No reason provided" : reason);
        message = message.replace("%appeal%", appeal);
        message = message.replace("%duration%", duration == null ? "" : duration);

        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public boolean addPunishment(String playerName, PunishmentType type, String reason, String operator, long duration) {
        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();
            PreparedStatement insert = connection.prepareStatement("INSERT INTO firepixel_punishments (player_name, punishment_type, reason, operator, duration, start_time, end_time, active) VALUES (?, ?, ?, ?, ?, ?, ?, 1)");
            long start = System.currentTimeMillis();
            long end = duration > 0 ? start + duration : 0;

            insert.setString(1, playerName);
            insert.setString(2, type.name());
            insert.setString(3, reason);
            insert.setString(4, operator);

            if (duration > 0) {
                insert.setLong(5, duration);
                insert.setLong(7, end);
            } else {
                insert.setNull(5, Types.BIGINT);
                insert.setNull(7, Types.BIGINT);
            }

            insert.setLong(6, start);
            insert.executeUpdate();
            insert.close();
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public boolean isPlayerBanned(String playerName) {
        return hasActive(playerName, "BAN", "TEMPBAN");
    }

    public boolean isPlayerMuted(String playerName) {
        return hasActive(playerName, "MUTE", "TEMPMUTE");
    }

    private boolean hasActive(String playerName, String... types) {
        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();

            for (String type : types) {
                PreparedStatement select = connection.prepareStatement("SELECT end_time FROM firepixel_punishments WHERE player_name = ? AND punishment_type = ? AND active = 1");
                select.setString(1, playerName);
                select.setString(2, type);
                ResultSet result = select.executeQuery();

                while (result.next()) {
                    long end = result.getLong("end_time");

                    if (end == 0 || end > System.currentTimeMillis()) {
                        result.close();
                        select.close();
                        return true;
                    }
                }

                result.close();
                select.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return false;
    }

    public boolean removePunishment(String playerName, String type) {
        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();
            PreparedStatement update = connection.prepareStatement("UPDATE firepixel_punishments SET active = 0 WHERE player_name = ? AND punishment_type = ? AND active = 1");
            update.setString(1, playerName);
            update.setString(2, type);
            int rows = update.executeUpdate();
            update.close();
            return rows > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    public void applyPunishment(String playerName, PunishmentType type, String reason, long duration) {
        Player player = Bukkit.getPlayer(playerName);

        if (player == null || !player.isOnline()) {
            return;
        }

        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());

        switch (type) {
            case BAN:
                player.kickPlayer(buildScreen(language, "ban", reason, null));
                break;
            case TEMPBAN:
                player.kickPlayer(buildScreen(language, "tempban", reason, formatDuration(duration)));
                break;
            case KICK:
                player.kickPlayer(buildScreen(language, "kick", reason, null));
                break;
            default:
                break;
        }
    }

    public void broadcast(String message, boolean silent) {
        if (silent) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("firepixel.staff")) {
                player.sendMessage(message);
            }
        }
    }

    public String formatDuration(long durationMs) {
        long seconds = durationMs / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + " day(s) " + (hours % 24) + " hour(s)";
        } else if (hours > 0) {
            return hours + " hour(s) " + (minutes % 60) + " minute(s)";
        } else if (minutes > 0) {
            return minutes + " minute(s) " + (seconds % 60) + " second(s)";
        } else {
            return seconds + " second(s)";
        }
    }

    public long parseDuration(String input) {
        try {
            if (input == null || input.isEmpty()) {
                return -1;
            }

            if (input.startsWith("#")) {
                input = input.substring(1);
            }

            long total;

            if (input.endsWith("s")) {
                total = Long.parseLong(input.substring(0, input.length() - 1)) * 1000;
            } else if (input.endsWith("m")) {
                total = Long.parseLong(input.substring(0, input.length() - 1)) * 60 * 1000;
            } else if (input.endsWith("h")) {
                total = Long.parseLong(input.substring(0, input.length() - 1)) * 60 * 60 * 1000;
            } else if (input.endsWith("d")) {
                total = Long.parseLong(input.substring(0, input.length() - 1)) * 24L * 60 * 60 * 1000;
            } else {
                total = Long.parseLong(input) * 60 * 1000;
            }

            return total;
        } catch (Exception exception) {
            return -1;
        }
    }
}