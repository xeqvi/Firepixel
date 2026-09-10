package net.firepixel.fun;

import net.firepixel.fun.command.DynamicCommand;
import net.firepixel.fun.command.LanguageCommand;
import net.firepixel.fun.command.SetLanguageCommand;
import net.firepixel.fun.command.SetSpawnCommand;
import net.firepixel.fun.command.SpawnCommand;
import net.firepixel.fun.listener.LanguageMenuListener;
import net.firepixel.fun.listener.PlayerJoinListener;
import net.firepixel.fun.listener.PlayerQuitListener;
import net.firepixel.fun.manager.DatabaseManager;
import net.firepixel.fun.manager.LanguageManager;
import net.firepixel.fun.manager.LanguageMenuManager;
import net.firepixel.fun.manager.PlayerDataManager;
import net.firepixel.fun.manager.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Firepixel extends JavaPlugin {

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private SpawnManager spawnManager;
    private LanguageManager languageManager;
    private LanguageMenuManager languageMenuManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        languageManager = new LanguageManager(this);
        languageManager.reload();

        databaseManager = new DatabaseManager(this);
        databaseManager.setup();

        playerDataManager = new PlayerDataManager(this, databaseManager.getDatabase());

        spawnManager = new SpawnManager(this);
        languageMenuManager = new LanguageMenuManager(this);

        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("language").setExecutor(new LanguageCommand(this));
        getCommand("setlanguage").setExecutor(new SetLanguageCommand(this));

        registerSpawnCommands();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new LanguageMenuListener(this), this);
    }

    private void registerSpawnCommands() {
        ConfigurationSection section = getConfig().getConfigurationSection("spawn.commands");

        if (section == null) {
            return;
        }

        CommandMap commandMap = getCommandMap();

        if (commandMap == null) {
            return;
        }

        for (String name : section.getKeys(false)) {
            String action = section.getString(name);
            commandMap.register(getName().toLowerCase(), new DynamicCommand(name, action));
        }
    }

    private CommandMap getCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
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

    public SpawnManager getSpawnManager() {
        return spawnManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    public LanguageMenuManager getLanguageMenuManager() {
        return languageMenuManager;
    }
}