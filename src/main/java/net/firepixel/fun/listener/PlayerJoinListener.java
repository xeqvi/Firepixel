package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Firepixel plugin;

    public PlayerJoinListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().load(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}