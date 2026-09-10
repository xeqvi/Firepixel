package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.menu.LanguageMenuHolder;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class LanguageMenuListener implements Listener {

    private static final int CLOSE_SLOT = 49;

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
        String worldName = player.getWorld() == null ? null : player.getWorld().getName();

        if (!plugin.getLanguageManager().isEnabledForWorld(worldName)) {
            String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
            player.sendMessage(plugin.getLanguageManager().getMessage(language, "language.world-disabled"));
            player.closeInventory();
            return;
        }

        int slot = event.getRawSlot();

        if (slot < 0 || slot >= event.getView().getTopInventory().getSize()) {
            return;
        }

        if (slot == CLOSE_SLOT) {
            player.closeInventory();
            return;
        }

        String code = ((LanguageMenuHolder) holder).getLanguage(slot);

        if (code == null) {
            return;
        }

        plugin.getPlayerDataManager().setLanguage(player.getUniqueId(), code);
        player.closeInventory();

        String message = plugin.getLanguageManager().getMessage(code, "language.changed");
        message = message.replace("%language%", plugin.getLanguageManager().getLanguageName(code));
        player.sendMessage(message);

        playSound(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView() != null && event.getView().getTopInventory() != null
                && event.getView().getTopInventory().getHolder() instanceof LanguageMenuHolder) {
            event.setCancelled(true);
        }
    }

    private void playSound(Player player) {
        if (player == null) {
            return;
        }

        try {
            player.playSound(player.getLocation(), Sound.valueOf("NOTE_PLING"), 1.0f, 1.0f);
            return;
        } catch (Throwable ignored) {
        }

        try {
            player.playSound(player.getLocation(), Sound.valueOf("CHICKEN_EGG_POP"), 1.0f, 1.0f);
        } catch (Throwable ignored) {
        }
    }
}