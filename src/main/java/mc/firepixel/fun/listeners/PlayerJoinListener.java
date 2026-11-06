package mc.firepixel.fun.listeners;


import mc.firepixel.fun.managers.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private PlayerDataManager playerDataManager;

    public void PlayerListener(PlayerDataManager playerDataManager) {
        this.playerDataManager = playerDataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        playerDataManager.handleJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playerDataManager.handleQuit(event.getPlayer());
    }

}
