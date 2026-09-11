package net.firepixel.fun;

import net.firepixel.fun.command.DynamicCommand;
import net.firepixel.fun.command.LanguageCommand;
import net.firepixel.fun.command.PunishCommand;
import net.firepixel.fun.command.RankCommand;
import net.firepixel.fun.command.ReportCommand;
import net.firepixel.fun.command.ScoreboardCommand;
import net.firepixel.fun.command.SetLanguageCommand;
import net.firepixel.fun.command.SetRankCommand;
import net.firepixel.fun.command.SetSpawnCommand;
import net.firepixel.fun.command.SpawnCommand;
import net.firepixel.fun.command.UnpunishCommand;
import net.firepixel.fun.listener.LanguageMenuListener;
import net.firepixel.fun.listener.PlayerJoinListener;
import net.firepixel.fun.listener.PlayerQuitListener;
import net.firepixel.fun.listener.PunishmentListener;
import net.firepixel.fun.listener.ReportMenuListener;
import net.firepixel.fun.manager.DatabaseManager;
import net.firepixel.fun.manager.JoinMessageManager;
import net.firepixel.fun.manager.LanguageManager;
import net.firepixel.fun.manager.LanguageMenuManager;
import net.firepixel.fun.manager.PlayerDataManager;
import net.firepixel.fun.manager.PunishmentManager;
import net.firepixel.fun.manager.RankManager;
import net.firepixel.fun.manager.ReportManager;
import net.firepixel.fun.manager.ScoreboardManager;
import net.firepixel.fun.manager.SpawnManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;

public class Firepixel extends JavaPlugin {

    private static Firepixel instance;

    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private SpawnManager spawnManager;
    private LanguageManager languageManager;
    private LanguageMenuManager languageMenuManager;
    private ReportManager reportManager;
    private PunishmentManager punishmentManager;
    private JoinMessageManager joinMessageManager;
    private RankManager rankManager;
    private ScoreboardManager scoreboardManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        languageManager = new LanguageManager(this);
        languageManager.reload();

        databaseManager = new DatabaseManager(this);
        databaseManager.setup();

        playerDataManager = new PlayerDataManager(this, databaseManager.getDatabase());

        rankManager = new RankManager(this);
        rankManager.reload();

        spawnManager = new SpawnManager(this);
        languageMenuManager = new LanguageMenuManager(this);
        reportManager = new ReportManager(this);
        punishmentManager = new PunishmentManager(this);
        joinMessageManager = new JoinMessageManager(this);
        scoreboardManager = new ScoreboardManager(this);
        scoreboardManager.start();

        getCommand("setspawn").setExecutor(new SetSpawnCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("language").setExecutor(new LanguageCommand(this));
        getCommand("setlanguage").setExecutor(new SetLanguageCommand(this));
        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("rank").setExecutor(new RankCommand(this));
        getCommand("setrank").setExecutor(new SetRankCommand(this));
        getCommand("scoreboard").setExecutor(new ScoreboardCommand(this));

        getCommand("ban").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.BAN));
        getCommand("tempban").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.TEMPBAN));
        getCommand("mute").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.MUTE));
        getCommand("tempmute").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.TEMPMUTE));
        getCommand("warn").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.WARN));
        getCommand("kick").setExecutor(new PunishCommand(this, PunishmentManager.PunishmentType.KICK));
        getCommand("unban").setExecutor(new UnpunishCommand(this, PunishmentManager.PunishmentType.BAN));
        getCommand("unmute").setExecutor(new UnpunishCommand(this, PunishmentManager.PunishmentType.MUTE));

        registerSpawnCommands();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new LanguageMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new ReportMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PunishmentListener(this), this);
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
        if (scoreboardManager != null) {
            scoreboardManager.stop();
        }

        if (databaseManager != null) {
            databaseManager.shutdown();
        }
    }

    public static Firepixel getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
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

    public ReportManager getReportManager() {
        return reportManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public JoinMessageManager getJoinMessageManager() {
        return joinMessageManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}