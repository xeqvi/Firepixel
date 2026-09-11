package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Firepixel plugin;

    public PlayerJoinListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(final PlayerJoinEvent event) {
        event.setJoinMessage(null);
        final Player player = event.getPlayer();
        plugin.getPlayerDataManager().load(player.getUniqueId(), player.getName());

        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            public void run() {
                if (player.isOnline()) {
                    plugin.getJoinMessageManager().handleJoin(player);
                }
            }
        }, 1L);

        if (plugin.getSpawnManager().isEnabledForWorld(player.getWorld().getName())) {
            Location spawn = plugin.getSpawnManager().getSpawn();

            if (spawn != null) {
                player.teleport(spawn);
            }
        }
    }
}