package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.database.Database;
import net.firepixel.fun.database.MySQLDatabase;
import net.firepixel.fun.database.SQLiteDatabase;

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