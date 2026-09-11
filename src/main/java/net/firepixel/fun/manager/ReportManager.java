package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.report.ReportMenuHolder;
import net.firepixel.fun.util.ColorUtil;
import net.firepixel.fun.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ReportManager {

    private static final int HEAD_SLOT = 4;
    private static final int INFO_SLOT = 48;
    private static final int CLOSE_SLOT = 49;
    private static final int BACK_SLOT = 49;

    private final Firepixel plugin;
    private final Map<UUID, String> targets = new HashMap<UUID, String>();
    private final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    public ReportManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void openReasonMenu(Player player) {
        openReasonMenu(player, null);
    }

    public void openReasonMenu(Player player, String target) {
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        String title = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.reason-title"));
        ReportMenuHolder holder = new ReportMenuHolder(ReportMenuHolder.Type.REASON);
        holder.setTarget(target);
        holder.setDirect(target != null && !target.trim().isEmpty());
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        if (target != null && !target.trim().isEmpty()) {
            inventory.setItem(HEAD_SLOT, buildTargetHead(target, language, null));
        }

        Set<String> keys = plugin.getLanguageManager().getKeys(language, "report.menu.reasons");

        for (String key : keys) {
            String base = "report.menu.reasons." + key;
            int slot = plugin.getLanguageManager().getInt(language, base + ".slot", -1);

            if (slot < 0 || slot >= 54) {
                continue;
            }

            Material material = Material.getMaterial(plugin.getLanguageManager().getString(language, base + ".material", "PAPER"));

            if (material == null) {
                material = Material.PAPER;
            }

            int data = plugin.getLanguageManager().getInt(language, base + ".data", 0);
            ItemStack item = new ItemStack(material, 1, (short) data);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, base + ".name")));

                List<String> lore = new ArrayList<String>();

                for (String line : plugin.getLanguageManager().getStringList(language, base + ".lore")) {
                    lore.add(ColorUtil.color(line));
                }

                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            inventory.setItem(slot, item);
            holder.setSlot(slot, key);
        }

        inventory.setItem(INFO_SLOT, buildInfoItem(language));
        inventory.setItem(CLOSE_SLOT, buildCloseItem(language));

        player.openInventory(inventory);
    }

    public void openPlayerMenu(Player player, String reasonKey) {
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        String title = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.player-title"));
        ReportMenuHolder holder = new ReportMenuHolder(ReportMenuHolder.Type.PLAYER);
        holder.setReason(reasonKey);
        Inventory inventory = Bukkit.createInventory(holder, 54, title);
        holder.setInventory(inventory);

        int slot = 0;

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target.getUniqueId().equals(player.getUniqueId())) {
                continue;
            }

            if (slot >= 45) {
                break;
            }

            inventory.setItem(slot, buildTargetHead(target.getName(), language, reasonKey));
            holder.setSlot(slot, target.getName());
            slot++;
        }

        inventory.setItem(BACK_SLOT, buildBackItem(language));

        player.openInventory(inventory);
    }

    public void openConfirmMenu(Player player, String target, String reasonKey, boolean direct) {
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        String title = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.confirm-title"));
        ReportMenuHolder holder = new ReportMenuHolder(ReportMenuHolder.Type.CONFIRM);
        holder.setTarget(target);
        holder.setReason(reasonKey);
        holder.setDirect(direct);
        Inventory inventory = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inventory);

        ItemStack confirm = new ItemStack(Material.STAINED_CLAY, 1, (short) 13);
        ItemMeta confirmMeta = confirm.getItemMeta();

        if (confirmMeta != null) {
            confirmMeta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.submit.name")));
            confirm.setItemMeta(confirmMeta);
        }

        ItemStack cancel = new ItemStack(Material.STAINED_CLAY, 1, (short) 14);
        ItemMeta cancelMeta = cancel.getItemMeta();

        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.cancel.name")));
            cancel.setItemMeta(cancelMeta);
        }

        inventory.setItem(11, confirm);
        inventory.setItem(13, buildTargetHead(target, language, reasonKey));
        inventory.setItem(15, cancel);
        inventory.setItem(22, buildBackItem(language));

        player.openInventory(inventory);
    }

    private ItemStack buildTargetHead(String target, String language, String reasonKey) {
        ItemStack item = MaterialUtil.createSkull();
        ItemMeta rawMeta = item.getItemMeta();

        if (!(rawMeta instanceof SkullMeta)) {
            return item;
        }

        SkullMeta meta = (SkullMeta) rawMeta;
        meta.setOwner(target);
        meta.setDisplayName(ColorUtil.color("&7/report &b" + target));

        List<String> lore = new ArrayList<String>();
        lore.add(ColorUtil.color("&7"));

        if (language != null && reasonKey != null) {
            String reasonName = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.reasons." + reasonKey + ".name"));
            lore.add(ColorUtil.color("&7Report &f" + target + " &7for " + reasonName));
        }

        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildInfoItem(String language) {
        ItemStack item = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.info.name")));
            List<String> lore = new ArrayList<String>();

            for (String line : plugin.getLanguageManager().getStringList(language, "report.menu.info.lore")) {
                lore.add(ColorUtil.color(line));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack buildBackItem(String language) {
        ItemStack item = new ItemStack(Material.ARROW, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.back.name")));
            List<String> lore = new ArrayList<String>();

            for (String line : plugin.getLanguageManager().getStringList(language, "report.menu.back.lore")) {
                lore.add(ColorUtil.color(line));
            }

            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private ItemStack buildCloseItem(String language) {
        ItemStack item = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.close.name")));
            item.setItemMeta(meta);
        }

        return item;
    }

    public void submit(Player reporter, String target, String reasonKey) {
        String language = plugin.getPlayerDataManager().getLanguage(reporter.getUniqueId());
        String reasonName = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.menu.reasons." + reasonKey + ".name"));

        if (isOnCooldown(reporter.getUniqueId())) {
            long remaining = getRemaining(reporter.getUniqueId());
            reporter.sendMessage(ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.cooldown").replace("%seconds%", String.valueOf(remaining))));
            return;
        }

        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();
            PreparedStatement insert = connection.prepareStatement("INSERT INTO firepixel_reports (reporter, reported, reason, timestamp, status) VALUES (?, ?, ?, ?, 'pending')");
            insert.setString(1, reporter.getName());
            insert.setString(2, target);
            insert.setString(3, reasonKey);
            insert.setLong(4, System.currentTimeMillis());
            insert.executeUpdate();
            insert.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return;
        }

        cooldowns.put(reporter.getUniqueId(), System.currentTimeMillis());

        List<String> submitted = plugin.getLanguageManager().getStringList(language, "report.submitted");

        for (String line : submitted) {
            reporter.sendMessage(ColorUtil.color(line.replace("%reason%", ChatColor.stripColor(reasonName))));
        }

        String notify = ColorUtil.color(plugin.getLanguageManager().getMessage(language, "report.staff-notify"));
        notify = notify.replace("%reporter%", reporter.getName()).replace("%reported%", target).replace("%reason%", ChatColor.stripColor(reasonName));

        for (Player staff : Bukkit.getOnlinePlayers()) {
            if (staff.hasPermission(plugin.getConfig().getString("report.staff-permission", "firepixel.report.staff"))) {
                staff.sendMessage(notify);
            }
        }
    }

    public boolean isOnCooldown(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }

        int seconds = plugin.getConfig().getInt("report.cooldown-seconds", 60);
        return System.currentTimeMillis() - cooldowns.get(uuid) < seconds * 1000L;
    }

    public long getRemaining(UUID uuid) {
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }

        int seconds = plugin.getConfig().getInt("report.cooldown-seconds", 60);
        long remaining = (seconds * 1000L) - (System.currentTimeMillis() - cooldowns.get(uuid));
        return Math.max(0, remaining / 1000);
    }

    public void setTarget(UUID uuid, String target) {
        targets.put(uuid, target);
    }

    public String getTarget(UUID uuid) {
        return targets.get(uuid);
    }

    public List<String> getPendingReporters() {
        List<String> reporters = new ArrayList<String>();

        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();
            PreparedStatement select = connection.prepareStatement("SELECT DISTINCT reporter FROM firepixel_reports WHERE status = 'pending'");
            ResultSet result = select.executeQuery();

            while (result.next()) {
                reporters.add(result.getString("reporter"));
            }

            result.close();
            select.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        return reporters;
    }

    public boolean accept(String reporter) {
        try {
            Connection connection = plugin.getDatabaseManager().getDatabase().getConnection();
            PreparedStatement update = connection.prepareStatement("UPDATE firepixel_reports SET status = 'accepted' WHERE reporter = ? AND status = 'pending'");
            update.setString(1, reporter);
            int rows = update.executeUpdate();
            update.close();
            return rows > 0;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
    }
}