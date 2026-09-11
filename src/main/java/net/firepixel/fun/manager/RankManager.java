package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RankManager {

    private final Firepixel plugin;
    private final File file;
    private YamlConfiguration configuration;
    private final Map<String, RankDefinition> ranks = new LinkedHashMap<String, RankDefinition>();

    public RankManager(Firepixel plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "ranks.yml");
    }

    public synchronized void reload() {
        try {
            if (!file.exists()) {
                plugin.saveResource("ranks.yml", false);
            }
        } catch (Exception ignored) {
        }

        if (!file.exists()) {
            createDefaultRanks();
            save();
            return;
        }

        configuration = YamlConfiguration.loadConfiguration(file);
        ranks.clear();

        ConfigurationSection section = configuration.getConfigurationSection("ranks");
        ConfigurationSection root = section != null ? section : configuration;

        for (String key : root.getKeys(false)) {
            ConfigurationSection rankSection = root.getConfigurationSection(key);

            if (rankSection == null) {
                continue;
            }

            RankDefinition definition = RankDefinition.fromSection(normalize(key), rankSection);
            ranks.put(definition.getId(), definition);
        }

        if (ranks.isEmpty()) {
            createDefaultRanks();
            save();
        }

        ensureDefaultRank();
    }

    public synchronized void save() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            configuration = new YamlConfiguration();

            for (RankDefinition definition : ranks.values()) {
                String path = definition.getId();
                configuration.set(path + ".tag", definition.getTag());
                configuration.set(path + ".priority", definition.getPriority());
                configuration.set(path + ".primary_group", definition.getPrimaryGroup());
                configuration.set(path + ".groups", new ArrayList<String>(definition.getGroups()));
                configuration.set(path + ".permissions", new ArrayList<String>(definition.getPermissions()));
                configuration.set(path + ".placeholders", new LinkedHashMap<String, String>(definition.getPlaceholders()));
            }

            configuration.save(file);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public Collection<RankDefinition> getRanks() {
        return Collections.unmodifiableCollection(ranks.values());
    }

    public List<RankDefinition> getSortedRanks() {
        List<RankDefinition> list = new ArrayList<RankDefinition>(ranks.values());
        Collections.sort(list, new Comparator<RankDefinition>() {
            public int compare(RankDefinition a, RankDefinition b) {
                return Integer.compare(b.getPriority(), a.getPriority());
            }
        });
        return list;
    }

    public RankDefinition getRank(String id) {
        RankDefinition definition = findRank(id);

        if (definition != null) {
            return definition;
        }

        RankDefinition fallback = ranks.get("default");

        if (fallback != null) {
            return fallback;
        }

        return ranks.isEmpty() ? null : ranks.values().iterator().next();
    }

    public boolean exists(String id) {
        return findRank(id) != null;
    }

    public RankDefinition ensureDefaultRank() {
        if (!ranks.containsKey("default")) {
            RankDefinition def = new RankDefinition("default");
            def.setTag("&7");
            def.setPriority(0);
            def.setPrimaryGroup("default");
            def.addGroup("default");
            def.addPermission("firepixel.rank.default");
            def.getPlaceholders().put("rank_id", "default");
            def.getPlaceholders().put("rank_name", "&7Default");
            def.getPlaceholders().put("rank_tag", "&7");
            def.getPlaceholders().put("rank_group", "default");
            ranks.put(def.getId(), def);
        }

        return ranks.get("default");
    }

    private RankDefinition findRank(String id) {
        String normalized = normalize(id);
        RankDefinition direct = ranks.get(normalized);

        if (direct != null) {
            return direct;
        }

        for (RankDefinition definition : ranks.values()) {
            if (definition.matches(normalized)) {
                return definition;
            }
        }

        return null;
    }

    public RankDefinition resolveRank(Player player) {
        if (player == null) {
            return getRank("default");
        }

        String luckPerms = detectLuckPermsPrimaryGroup(player);
        RankDefinition byLuckPerms = findRank(luckPerms);

        if (byLuckPerms != null) {
            return byLuckPerms;
        }

        return getRank("default");
    }

    public String resolveRankId(Player player) {
        RankDefinition definition = resolveRank(player);
        return definition == null ? "default" : definition.getId();
    }

    public String detectLuckPermsPrimaryGroup(Player player) {
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
            Object user = userManager.getClass().getMethod("getUser", UUID.class).invoke(userManager, player.getUniqueId());

            if (user == null) {
                return "default";
            }

            Object cachedData = user.getClass().getMethod("getCachedData").invoke(user);
            Object metaData = cachedData.getClass().getMethod("getMetaData").invoke(cachedData);
            Object group = metaData.getClass().getMethod("getPrimaryGroup").invoke(metaData);
            return group == null ? "default" : group.toString();
        } catch (Throwable ignored) {
        }

        return "default";
    }

    public Map<String, String> getPlaceholderMap(Player player) {
        Map<String, String> placeholders = new LinkedHashMap<String, String>();
        RankDefinition definition = resolveRank(player);

        if (definition != null) {
            placeholders.putAll(definition.getPlaceholders());
            placeholders.put("rank_id", definition.getId());
            placeholders.put("rank_name", definition.getPlaceholders().containsKey("rank_name") ? definition.getPlaceholders().get("rank_name") : getRankName(definition.getId()));
            placeholders.put("rank_tag", definition.getTag());
            placeholders.put("rank_group", definition.getPrimaryGroup());
            placeholders.put("rank_priority", String.valueOf(definition.getPriority()));
            placeholders.put("rank_display", getDisplayName(player));
            placeholders.put("rank", definition.getTag());
            placeholders.put("prefix", definition.getTag());
        }

        return placeholders;
    }

    public String applyRankPlaceholders(Player player, String text) {
        if (text == null) {
            return "";
        }

        RankDefinition definition = resolveRank(player);
        String result = text;

        if (definition != null) {
            for (Map.Entry<String, String> entry : definition.getPlaceholders().entrySet()) {
                String value = entry.getValue() == null ? "" : entry.getValue();
                result = result.replace("%" + entry.getKey() + "%", value);
                result = result.replace("{" + entry.getKey() + "}", value);
            }

            result = result.replace("%rank%", definition.getTag());
            result = result.replace("{rank}", definition.getTag());
            result = result.replace("%rank_name%", getRankName(definition.getId()));
            result = result.replace("%rank_tag%", definition.getTag());
            result = result.replace("%rank_group%", definition.getPrimaryGroup());
            result = result.replace("%rank_priority%", String.valueOf(definition.getPriority()));
            result = result.replace("%rank_display%", getDisplayName(player));
        }

        return result;
    }

    public String getRankTag(Player player) {
        RankDefinition definition = resolveRank(player);
        return definition != null ? definition.getTag() : "";
    }

    public String getRankTag(String rawGroup) {
        RankDefinition definition = findRank(rawGroup);

        if (definition != null) {
            return definition.getTag();
        }

        String group = normalize(rawGroup);

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
        if (group.equals("mvpplusplus") || group.equals("mvp++") || group.equals("mvppp")) return "&b[MVP&f++&b]";
        if (group.equals("mvpplus") || group.equals("mvp+")) return "&b[MVP&f+&b]";
        if (group.equals("mvp")) return "&b[MVP]";
        if (group.equals("vipplus") || group.equals("vip+")) return "&a[VIP&6+&a]";
        if (group.equals("vip")) return "&a[VIP]";
        return "";
    }

    public String getRankName(Player player) {
        RankDefinition definition = resolveRank(player);
        return definition != null ? definition.getDisplayName() : "&7Default";
    }

    public String getRankName(String rankId) {
        RankDefinition rank = getRank(rankId);
        return rank == null ? "&7Default" : rank.getDisplayName();
    }

    public String getDisplayName(Player player) {
        if (player == null) {
            return "";
        }

        RankDefinition definition = resolveRank(player);

        if (definition == null || "default".equalsIgnoreCase(definition.getId())) {
            return "&7" + player.getName();
        }

        String tag = definition.getTag();

        if (tag == null || tag.trim().isEmpty()) {
            return "&7" + player.getName();
        }

        return tag + " " + player.getName();
    }

    public Tier getTier(String group) {
        String normalized = normalize(group);

        if (normalized.equals("owner")) return Tier.OWNER;
        if (normalized.equals("coowner")) return Tier.COOWNER;
        if (normalized.equals("founder")) return Tier.FOUNDER;
        if (normalized.equals("mainadmin")) return Tier.MAINADMIN;
        if (normalized.equals("admin")) return Tier.ADMIN;
        if (normalized.equals("mod")) return Tier.MOD;
        if (normalized.equals("gm")) return Tier.GM;
        if (normalized.equals("helper")) return Tier.HELPER;
        if (normalized.equals("jrhelper")) return Tier.JRHELPER;
        if (normalized.equals("youtube")) return Tier.YOUTUBE;
        if (normalized.equals("mvpplusplus") || normalized.equals("mvp++") || normalized.equals("mvppp")) return Tier.MVP_PLUS_PLUS;
        if (normalized.equals("mvpplus") || normalized.equals("mvp+")) return Tier.MVP_PLUS;
        if (normalized.equals("mvp")) return Tier.MVP;
        if (normalized.equals("vipplus") || normalized.equals("vip+")) return Tier.VIP_PLUS;
        if (normalized.equals("vip")) return Tier.VIP;
        return Tier.DEFAULT;
    }

    private void createDefaultRanks() {
        ranks.clear();
        addDefault("default", "&7", 0, "default", new String[] {"firepixel.rank.default", "firepixel.scoreboard"}, "&7Default");
        addDefault("vip", "&a[VIP] ", 10, "vip", new String[] {"firepixel.rank.vip", "firepixel.chat.bypass", "firepixel.fly"}, "&aVIP");
        addDefault("vipplus", "&a[VIP&6+&a] ", 20, "vipplus", new String[] {"firepixel.rank.vipplus", "firepixel.chat.bypass", "firepixel.fly"}, "&aVIP&6+");
        addDefault("mvp", "&b[MVP] ", 30, "mvp", new String[] {"firepixel.rank.mvp", "firepixel.chat.bypass", "firepixel.fly"}, "&bMVP");
        addDefault("mvpplus", "&b[MVP&f+&b] ", 40, "mvpplus", new String[] {"firepixel.rank.mvpplus", "firepixel.chat.bypass", "firepixel.fly"}, "&bMVP&f+");
        addDefault("mvpplusplus", "&b[MVP&f++&b] ", 50, "mvpplusplus", new String[] {"firepixel.rank.mvpplusplus", "firepixel.chat.bypass", "firepixel.fly"}, "&bMVP&f++");
        addDefault("youtube", "&c[&fYOUTUBE&c] ", 60, "youtube", new String[] {"firepixel.rank.youtube", "firepixel.chat.bypass", "firepixel.fly"}, "&fYOUTUBE");
        addDefault("jrhelper", "&9[JR HELPER] ", 70, "jrhelper", new String[] {"firepixel.rank.jrhelper", "firepixel.chat.bypass", "firepixel.report.staff"}, "&9JR HELPER");
        addDefault("helper", "&9[HELPER] ", 80, "helper", new String[] {"firepixel.rank.helper", "firepixel.chat.bypass", "firepixel.report.staff"}, "&9HELPER");
        addDefault("gm", "&2[GM] ", 90, "gm", new String[] {"firepixel.rank.gm", "firepixel.chat.bypass", "firepixel.report.staff"}, "&2GM");
        addDefault("mod", "&2[MOD] ", 95, "mod", new String[] {"firepixel.rank.mod", "firepixel.chat.bypass", "firepixel.report.staff"}, "&2MOD");
        addDefault("admin", "&c[ADMIN] ", 100, "admin", new String[] {"firepixel.rank.admin", "firepixel.chat.bypass", "firepixel.report.staff"}, "&cADMIN");
        addDefault("mainadmin", "&c[MAIN ADMIN] ", 110, "mainadmin", new String[] {"firepixel.rank.mainadmin", "firepixel.chat.bypass", "firepixel.report.staff"}, "&cMAIN ADMIN");
        addDefault("founder", "&c[FOUNDER] ", 120, "founder", new String[] {"firepixel.rank.founder", "firepixel.chat.bypass", "firepixel.report.staff"}, "&cFOUNDER");
        addDefault("coowner", "&c[CO OWNER] ", 130, "coowner", new String[] {"firepixel.rank.coowner", "firepixel.chat.bypass", "firepixel.report.staff"}, "&cCO OWNER");
        addDefault("owner", "&c[OWNER] ", 140, "owner", new String[] {"*"}, "&cOWNER");
        save();
    }

    private void addDefault(String id, String tag, int priority, String primaryGroup, String[] permissions, String displayName) {
        RankDefinition def = new RankDefinition(id);
        def.setTag(tag);
        def.setPriority(priority);
        def.setPrimaryGroup(primaryGroup);
        def.addGroup(primaryGroup);

        if (permissions != null) {
            for (String permission : permissions) {
                def.addPermission(permission);
            }
        }

        def.setPlaceholder("rank_id", id);
        def.setPlaceholder("rank_name", displayName);
        def.setPlaceholder("rank_tag", tag);
        def.setPlaceholder("rank_group", primaryGroup);
        ranks.put(def.getId(), def);
    }

    private String normalize(String id) {
        if (id == null) {
            return "default";
        }

        String normalized = id.trim().toLowerCase(Locale.ROOT);
        return normalized.isEmpty() ? "default" : normalized;
    }

    public static class RankDefinition {
        private final String id;
        private String tag = "";
        private int priority = 0;
        private String primaryGroup = "default";
        private final Set<String> groups = new LinkedHashSet<String>();
        private final Set<String> permissions = new LinkedHashSet<String>();
        private final Map<String, String> placeholders = new LinkedHashMap<String, String>();

        private RankDefinition(String id) {
            this.id = id == null ? "default" : id.trim().toLowerCase(Locale.ROOT);
        }

        public static RankDefinition fromSection(String id, ConfigurationSection section) {
            RankDefinition def = new RankDefinition(id);
            def.setTag(section.getString("tag", section.getString("display", "")));
            def.setPriority(section.getInt("priority", 0));
            def.setPrimaryGroup(section.getString("primary_group", id));

            List<String> groupList = section.getStringList("groups");

            if (groupList != null) {
                for (String group : groupList) {
                    def.addGroup(group);
                }
            }

            List<String> permissionList = section.getStringList("permissions");

            if (permissionList != null) {
                for (String permission : permissionList) {
                    def.addPermission(permission);
                }
            }

            ConfigurationSection placeholderSection = section.getConfigurationSection("placeholders");

            if (placeholderSection != null) {
                for (String key : placeholderSection.getKeys(false)) {
                    def.setPlaceholder(key, placeholderSection.getString(key, ""));
                }
            }

            if (def.groups.isEmpty()) {
                def.addGroup(def.primaryGroup);
            }

            return def;
        }

        public String getId() {
            return id;
        }

        public String getTag() {
            return tag == null ? "" : tag;
        }

        public void setTag(String tag) {
            this.tag = tag == null ? "" : tag;
        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public String getPrimaryGroup() {
            return primaryGroup;
        }

        public void setPrimaryGroup(String primaryGroup) {
            this.primaryGroup = primaryGroup == null || primaryGroup.trim().isEmpty() ? "default" : primaryGroup.trim().toLowerCase(Locale.ROOT);

            if (groups.isEmpty()) {
                groups.add(this.primaryGroup);
            }
        }

        public Set<String> getGroups() {
            return Collections.unmodifiableSet(groups);
        }

        public boolean addGroup(String group) {
            if (group == null || group.trim().isEmpty()) {
                return false;
            }

            return groups.add(group.trim().toLowerCase(Locale.ROOT));
        }

        public Set<String> getPermissions() {
            return Collections.unmodifiableSet(permissions);
        }

        public boolean addPermission(String permission) {
            if (permission == null || permission.trim().isEmpty()) {
                return false;
            }

            return permissions.add(permission.trim());
        }

        public Map<String, String> getPlaceholders() {
            return placeholders;
        }

        public void setPlaceholder(String key, String value) {
            if (key == null || key.trim().isEmpty()) {
                return;
            }

            placeholders.put(key, value == null ? "" : value);
        }

        public boolean matches(String group) {
            String normalized = group == null ? "" : group.trim().toLowerCase(Locale.ROOT);

            if (normalized.isEmpty()) {
                return false;
            }

            if (id.equals(normalized) || primaryGroup.equals(normalized)) {
                return true;
            }

            return groups.contains(normalized);
        }

        public String getDisplayName() {
            String value = placeholders.get("rank_name");

            if (value != null && !value.trim().isEmpty()) {
                return value;
            }

            if ("default".equalsIgnoreCase(id)) {
                return "&7Default";
            }

            return id.toUpperCase(Locale.ROOT);
        }
    }

    public enum Tier {
        DEFAULT,
        VIP,
        VIP_PLUS,
        MVP,
        MVP_PLUS,
        MVP_PLUS_PLUS,
        YOUTUBE,
        JRHELPER,
        HELPER,
        GM,
        MOD,
        ADMIN,
        MAINADMIN,
        FOUNDER,
        COOWNER,
        OWNER
    }
}