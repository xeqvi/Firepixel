package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Firepixel plugin;

    public PlayerQuitListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().save(event.getPlayer().getUniqueId());
    }
}