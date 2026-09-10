package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.menu.LanguageMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class LanguageMenuManager {

    private final Firepixel plugin;

    public LanguageMenuManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        LanguageMenuHolder holder = new LanguageMenuHolder();
        Inventory inventory = Bukkit.createInventory(holder, 27, plugin.getLanguageManager().getMessage(language, "language.menu.title"));
        holder.setInventory(inventory);

        List<String> codes = plugin.getLanguageManager().getSupportedLanguages();
        int slot = 0;

        for (String code : codes) {
            if (slot >= 26) {
                break;
            }

            ItemStack item = new ItemStack(Material.WOOL, 1, DyeColor.LIME.getData());
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&a" + plugin.getLanguageManager().getLanguageName(code)));

            List<String> lore = new ArrayList<String>();
            lore.add(plugin.getLanguageManager().getMessage(language, "language.menu.item-lore"));
            meta.setLore(lore);

            item.setItemMeta(meta);
            inventory.setItem(slot, item);
            holder.setLanguage(slot, code);
            slot++;
        }

        player.openInventory(inventory);
    }
}