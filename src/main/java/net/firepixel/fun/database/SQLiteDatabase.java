package net.firepixel.fun.database;

import net.firepixel.fun.Firepixel;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteDatabase implements Database {

    private final Firepixel plugin;
    private Connection connection;

    public SQLiteDatabase(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String file = plugin.getConfig().getString("database.table", "firepixel.db");

        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + new File(plugin.getDataFolder(), file).getAbsolutePath());
        } catch (ClassNotFoundException | SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void disconnect() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public void createTable(String table) {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + table + " (uuid VARCHAR(36) PRIMARY KEY, name VARCHAR(16), first_join BIGINT, last_join BIGINT, language VARCHAR(8), rank VARCHAR(32))");
            statement.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}