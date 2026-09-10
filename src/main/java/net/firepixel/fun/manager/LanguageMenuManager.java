package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.menu.LanguageMenuHolder;
import net.firepixel.fun.util.MaterialUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageMenuManager {

    private static final int SIZE = 54;
    private static final int[] LANGUAGE_SLOTS = new int[] {10, 11, 12, 13, 14, 15, 16};
    private static final int HELP_SLOT = 50;
    private static final int CLOSE_SLOT = 49;

    private static final Map<String, String> LANGUAGE_TEXTURES = new HashMap<String, String>();

    static {
        LANGUAGE_TEXTURES.put("en", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGNhYzk3NzRkYTEyMTcyNDg1MzJjZTE0N2Y3ODMxZjY3YTEyZmRjY2ExY2YwY2I0YjM4NDhkZTZiYzk0YjQifX19");
        LANGUAGE_TEXTURES.put("ru", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZlYWZlZjk4MGQ2MTE3ZGFiZTg5ODJhYzRiNDUwOTg4N2UyYzQ2MjFmNmE4ZmU1YzliNzM1YTgzZDc3NWFkIn19fQ==");
        LANGUAGE_TEXTURES.put("nl", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIzY2YyMTBlZGVhMzk2ZjJmNWRmYmNlZDY5ODQ4NDM0ZjkzNDA0ZWVmZWFiZjU0YjIzYzA3M2IwOTBhZGYifX19");
        LANGUAGE_TEXTURES.put("zh", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Y5YmMwMzVjZGM4MGYxYWI1ZTExOThmMjlmM2FkM2ZkZDJiNDJkOWE2OWFlYjY0ZGU5OTA2ODE4MDBiOThkYyJ9fX0=");
        LANGUAGE_TEXTURES.put("ja", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZjMmNhNzIzODY2NmFlMWI5ZGQ5ZGFhM2Q0ZmM4MjlkYjIyNjA5ZmI1NjkzMTJkZWMxZmIwYzhkNmRkNmMxZCJ9fX0=");
        LANGUAGE_TEXTURES.put("ko", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmMxYmU1ZjEyZjQ1ZTQxM2VkYTU2ZjNkZTk0ZTA4ZDkwZWRlOGUzMzljN2IxZThmMzI3OTczOTBlOWE1ZiJ9fX0=");
        LANGUAGE_TEXTURES.put("pt", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWJkNTFmNDY5M2FmMTc0ZTZmZTE5NzkyMzNkMjNhNDBiYjk4NzM5OGUzODkxNjY1ZmFmZDJiYTU2N2I1YTUzYSJ9fX0=");
    }

    private final Firepixel plugin;

    public LanguageMenuManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void open(Player viewer) {
        if (viewer == null) {
            return;
        }

        String worldName = viewer.getWorld() == null ? null : viewer.getWorld().getName();

        if (!plugin.getLanguageManager().isEnabledForWorld(worldName)) {
            String language = plugin.getPlayerDataManager().getLanguage(viewer.getUniqueId());
            viewer.sendMessage(plugin.getLanguageManager().getMessage(language, "language.world-disabled"));
            return;
        }

        String viewerLanguage = plugin.getPlayerDataManager().getLanguage(viewer.getUniqueId());
        String title = plugin.getLanguageManager().getMessage(viewerLanguage, "language.menu.title");

        LanguageMenuHolder holder = new LanguageMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, SIZE, title);
        holder.setInventory(inventory);

        inventory.setItem(CLOSE_SLOT, buildCloseItem(viewerLanguage));
        inventory.setItem(HELP_SLOT, buildHelpItem(viewerLanguage));

        List<String> supported = plugin.getLanguageManager().getSupportedLanguages();

        for (int i = 0; i < LANGUAGE_SLOTS.length && i < supported.size(); i++) {
            String code = supported.get(i);
            inventory.setItem(LANGUAGE_SLOTS[i], createLanguageHead(viewerLanguage, code));
            holder.setLanguage(LANGUAGE_SLOTS[i], code);
        }

        viewer.openInventory(inventory);
    }

    private ItemStack createLanguageHead(String viewerLanguage, String code) {
        ItemStack item = MaterialUtil.createSkull();
        ItemMeta rawMeta = item.getItemMeta();

        if (!(rawMeta instanceof SkullMeta)) {
            return item;
        }

        SkullMeta meta = (SkullMeta) rawMeta;

        String texture = plugin.getLanguageManager().getMessage(viewerLanguage, "language.menu.items." + code + ".texture");

        if (texture == null || texture.trim().isEmpty()) {
            texture = LANGUAGE_TEXTURES.get(code);
        }

        if (texture != null && !texture.trim().isEmpty()) {
            MaterialUtil.applyTexture(meta, texture);
        }

        String name = plugin.getLanguageManager().getMessage(viewerLanguage, "language.menu.items." + code + ".name");

        if (name == null || name.trim().isEmpty()) {
            name = "&a" + plugin.getLanguageManager().getLanguageName(code);
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = plugin.getLanguageManager().getStringList(viewerLanguage, "language.menu.items." + code + ".lore");

        if (lore.isEmpty()) {
            lore = new ArrayList<String>();
            lore.add("&7Click to choose this language.");
        }

        boolean selected = viewerLanguage != null && viewerLanguage.equalsIgnoreCase(code);
        List<String> rendered = new ArrayList<String>();

        for (String line : lore) {
            rendered.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        if (rendered.isEmpty()) {
            rendered.add(ChatColor.translateAlternateColorCodes('&', "&7Click to choose this language."));
        }

        rendered.set(rendered.size() - 1, ChatColor.translateAlternateColorCodes('&', selected ? "&aSelected!" : "&eClick to change your language!"));
        meta.setLore(rendered);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildHelpItem(String viewerLanguage) {
        ItemStack item = new ItemStack(Material.BOOK, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        String name = plugin.getLanguageManager().getMessage(viewerLanguage, "language.menu.help.name");

        if (name == null || name.trim().isEmpty()) {
            name = "&aHelp us Translate Firepixel";
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = plugin.getLanguageManager().getStringList(viewerLanguage, "language.menu.help.lore");

        if (lore.isEmpty()) {
            lore = new ArrayList<String>();
            lore.add("&7We have added a way for you to help");
            lore.add("&7us translate Firepixel into even");
            lore.add("&7more languages!");
            lore.add("&eClick this icon for the help link.");
        }

        List<String> rendered = new ArrayList<String>();

        for (String line : lore) {
            rendered.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(rendered);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack buildCloseItem(String viewerLanguage) {
        ItemStack item = new ItemStack(Material.BARRIER, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) {
            return item;
        }

        String name = plugin.getLanguageManager().getMessage(viewerLanguage, "language.menu.close.name");

        if (name == null || name.trim().isEmpty()) {
            name = "&cClose";
        }

        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

        List<String> lore = plugin.getLanguageManager().getStringList(viewerLanguage, "language.menu.close.lore");

        if (lore.isEmpty()) {
            lore = new ArrayList<String>();
            lore.add("&7Close the menu.");
            lore.add("");
            lore.add("&eClick to close.");
        }

        List<String> rendered = new ArrayList<String>();

        for (String line : lore) {
            rendered.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        meta.setLore(rendered);
        item.setItemMeta(meta);

        return item;
    }
}