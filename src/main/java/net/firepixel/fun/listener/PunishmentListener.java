package net.firepixel.fun.listener;

import net.firepixel.fun.Firepixel;
import org.bukkit.ChatColor;
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
        String language = plugin.getLanguageManager().getDefaultLanguage();
        event.setKickMessage(plugin.getPunishmentManager().buildScreen(language, "ban", "Banned", null));
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getPunishmentManager().isPlayerMuted(player.getName())) {
            return;
        }

        event.setCancelled(true);
        String language = plugin.getPlayerDataManager().getLanguage(player.getUniqueId());
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getLanguageManager().getMessage(language, "punishments.screens.mute")));
    }
}