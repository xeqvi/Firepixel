package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.database.Database;
import net.firepixel.fun.database.MySQLDatabase;
import net.firepixel.fun.database.SQLiteDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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
        createModuleTables(type);
    }

    private void createModuleTables(String type) {
        Connection connection = database.getConnection();

        if (connection == null) {
            return;
        }

        String autoIncrement = type.equalsIgnoreCase("mysql") ? "AUTO_INCREMENT" : "AUTOINCREMENT";

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS firepixel_reports (id INTEGER PRIMARY KEY " + autoIncrement + ", reporter VARCHAR(16) NOT NULL, reported VARCHAR(16) NOT NULL, reason TEXT NOT NULL, timestamp BIGINT NOT NULL, status VARCHAR(20) DEFAULT 'pending')");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS firepixel_punishments (id INTEGER PRIMARY KEY " + autoIncrement + ", player_name VARCHAR(16) NOT NULL, punishment_type VARCHAR(20) NOT NULL, reason TEXT NOT NULL, operator VARCHAR(16) NOT NULL, duration BIGINT, start_time BIGINT NOT NULL, end_time BIGINT, active INTEGER DEFAULT 1)");
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
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