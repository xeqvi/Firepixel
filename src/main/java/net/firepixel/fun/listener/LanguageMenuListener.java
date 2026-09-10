package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.menu.LanguageMenuHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;

public class LanguageMenuListener implements Listener {

    private final Firepixel plugin;

    public LanguageMenuListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof LanguageMenuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String code = ((LanguageMenuHolder) holder).getLanguage(event.getSlot());

        if (code == null) {
            return;
        }

        plugin.getPlayerDataManager().setLanguage(player.getUniqueId(), code);
        player.closeInventory();
        player.sendMessage(plugin.getLanguageManager().getMessage(code, "language.changed").replace("%language%", plugin.getLanguageManager().getLanguageName(code)));
    }
}