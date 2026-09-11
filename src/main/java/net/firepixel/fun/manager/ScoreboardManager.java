package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.util.ColorUtil;
import net.firepixel.fun.util.PlaceholderUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ScoreboardManager {

    private static final String SIDEBAR_OBJECTIVE = "fp_sidebar";

    private final Firepixel plugin;
    private final Map<UUID, Boolean> toggles = new HashMap<UUID, Boolean>();
    private BukkitTask task;

    public ScoreboardManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();

        long interval = plugin.getConfig().getLong("scoreboard.update_interval_ticks", 100L);

        if (interval < 1L) {
            interval = 100L;
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            public void run() {
                refreshAll();
            }
        }, interval, interval);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public boolean isToggled(UUID uuid) {
        Boolean value = toggles.get(uuid);
        return value == null || value;
    }

    public boolean toggle(Player player) {
        boolean enabled = !isToggled(player.getUniqueId());
        toggles.put(player.getUniqueId(), enabled);
        refreshPlayer(player);
        return enabled;
    }

    public void refreshAll() {
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            refreshPlayer(viewer);
        }
    }

    public void refreshPlayer(Player viewer) {
        if (viewer == null || !viewer.isOnline()) {
            return;
        }

        org.bukkit.scoreboard.ScoreboardManager manager = Bukkit.getScoreboardManager();

        if (manager == null) {
            return;
        }

        Scoreboard board = manager.getNewScoreboard();
        registerTeams(board);

        if (isSidebarEnabled(viewer)) {
            applySidebar(board, viewer);
        }

        viewer.setScoreboard(board);
    }

    private void registerTeams(Scoreboard board) {
        for (Player target : plugin.getServer().getOnlinePlayers()) {
            String teamName = buildTeamName(target.getUniqueId());
            Team team = board.getTeam(teamName);

            if (team == null) {
                team = board.registerNewTeam(teamName);
            }

            String prefixTemplate = plugin.getConfig().getString("tab.player_prefix", "");
            String prefix;

            if (prefixTemplate != null && ("%rank%".equalsIgnoreCase(prefixTemplate.trim()) || "{rank}".equalsIgnoreCase(prefixTemplate.trim()) || "%prefix%".equalsIgnoreCase(prefixTemplate.trim()))) {
                prefix = plugin.getRankManager().getRankTag(target);
            } else {
                prefix = PlaceholderUtil.resolve(target, prefixTemplate);
            }

            String suffix = PlaceholderUtil.resolve(target, plugin.getConfig().getString("tab.player_suffix", ""));
            prefix = ColorUtil.color(prefix);
            suffix = ColorUtil.color(suffix);

            if (prefix.length() > 16) {
                prefix = prefix.substring(0, 16);
            }

            if (suffix.length() > 16) {
                suffix = suffix.substring(0, 16);
            }

            team.setPrefix(prefix);
            team.setSuffix(suffix);

            try {
                team.addEntry(target.getName());
            } catch (Throwable ignored) {
            }
        }
    }

    private void applySidebar(Scoreboard board, Player viewer) {
        String language = plugin.getPlayerDataManager().getLanguage(viewer.getUniqueId());
        String title = ColorUtil.color(PlaceholderUtil.resolve(viewer, plugin.getLanguageManager().getMessage(language, "scoreboard.title")));

        if (title.length() > 32) {
            title = title.substring(0, 32);
        }

        Objective objective = board.getObjective(SIDEBAR_OBJECTIVE);

        if (objective != null) {
            objective.unregister();
        }

        objective = board.registerNewObjective(SIDEBAR_OBJECTIVE, "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(title);

        List<String> lines = plugin.getLanguageManager().getStringList(language, "scoreboard.lines");

        if (lines == null || lines.isEmpty()) {
            return;
        }

        int size = Math.min(lines.size(), 15);

        for (int i = 0; i < size; i++) {
            String line = lines.get(i);
            String resolved = ColorUtil.color(PlaceholderUtil.resolve(viewer, line));
            String entry = makeUniqueEntry(resolved, i);
            objective.getScore(entry).setScore(size - i);
        }
    }

    private boolean isSidebarEnabled(Player player) {
        if (!plugin.getConfig().getBoolean("scoreboard.enabled", true)) {
            return false;
        }

        if (!isToggled(player.getUniqueId())) {
            return false;
        }

        List<String> worlds = plugin.getConfig().getStringList("scoreboard.scoreboard_enabled_worlds");

        if (worlds == null || worlds.isEmpty()) {
            return true;
        }

        for (String entry : worlds) {
            if (entry.equals("*") || entry.equalsIgnoreCase(player.getWorld().getName())) {
                return true;
            }
        }

        return false;
    }

    private String buildTeamName(UUID uuid) {
        String cleaned = uuid.toString().replace("-", "");

        if (cleaned.length() > 14) {
            cleaned = cleaned.substring(0, 14);
        }

        return "fp" + cleaned;
    }

    private String makeUniqueEntry(String text, int index) {
        String value = text == null ? "" : text;

        if (value.length() > 38) {
            value = value.substring(0, 38);
        }

        ChatColor[] colors = ChatColor.values();
        return value + colors[index % colors.length];
    }
}