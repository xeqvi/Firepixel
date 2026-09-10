package net.firepixel.fun;

import net.firepixel.fun.database.DatabaseManager;
import net.firepixel.fun.listener.PlayerJoinListener;
import net.firepixel.fun.listener.PlayerQuitListener;
import net.firepixel.fun.player.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Firepixel extends JavaPlugin {

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        databaseManager = new DatabaseManager(this);
        databaseManager.setup();

        playerDataManager = new PlayerDataManager(this, databaseManager.getDatabase());

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}