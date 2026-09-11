package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.manager.PunishmentManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class PunishmentListener implements Listener {

    private final Firepixel plugin;

    public PunishmentListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getPunishmentManager().isPlayerBanned(player.getName())) {
            return;
        }

        event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
        event.setKickMessage(plugin.getPunishmentManager().getScreen(PunishmentManager.PunishmentType.BAN, "Banned", 0));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getPunishmentManager().isPlayerMuted(player.getName())) {
            return;
        }

        event.setCancelled(true);
        player.sendMessage(plugin.getPunishmentManager().getScreen(PunishmentManager.PunishmentType.MUTE, null, 0));
    }
}