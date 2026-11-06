package mc.firepixel.fun;

import mc.firepixel.fun.database.DatabaseManager;
import mc.firepixel.fun.listeners.PlayerListener;
import  mc.firepixel.fun.managers.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Firepixel extends JavaPlugin {

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
          databaseManager = new DatabaseManager(this);
          databaseManager.connect();
          databaseManager.createTables();

          playerDataManager = new PlayerDataManager(this, databaseManager);
          getServer().getPluginManager().registerEvents(
                  new PlayerListener(playerDataManager), this
          );

          playerDataManager.startAutoSave();

          getLogger().info("Firepixel Has been enabled!");
        } catch (Exception exception) {
            getLogger().severe("Could not initialize Firepixel!");
            exception.printStackTrace();

            if (playerDataManager != null) {
                playerDataManager.shutdown();
            }

            if (databaseManager != null) {
                databaseManager.close();
            }

            getServer().getPluginManager().disablePlugin(this);

        }
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.shutdown();
        }

        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("Firepixel has been disabled!");
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
}
