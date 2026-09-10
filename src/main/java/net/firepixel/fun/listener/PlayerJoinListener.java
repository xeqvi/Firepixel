package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import org.bukkit.Location;
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

        if (!plugin.getSpawnManager().isEnabledForWorld(event.getPlayer().getWorld().getName())) {
            return;
        }

        Location spawn = plugin.getSpawnManager().getSpawn();

        if (spawn != null) {
            event.getPlayer().teleport(spawn);
        }

        event.getPlayer().sendMessage("");
    }
}