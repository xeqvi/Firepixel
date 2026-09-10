package net.firepixel.fun.database;

import net.firepixel.fun.Firepixel;

public class DatabaseManager {

    private final Firepixel plugin;
    private Database database;

    public DatabaseManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void setup() {
        String type = plugin.getConfig().getString("database.Storage", "sqlite");

        if (type.equalsIgnoreCase("mysql")) {
            database = new MySQLDatabase(plugin);
        } else {
            database = new SQLiteDatabase(plugin);
        }

        database.connect();
        database.createTable("firepixel_players");
    }

    public void shutdown() {
        if (database != null) {
            database.disconnect();
        }
    }

    public Database getDatabase() {
        return database;
    }
}