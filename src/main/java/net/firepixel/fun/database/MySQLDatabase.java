package net.firepixel.fun.database;

import net.firepixel.fun.Firepixel;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class MySQLDatabase implements Database {

    private final Firepixel plugin;
    private Connection connection;

    public MySQLDatabase(Firepixel plugin) {
        this.plugin = plugin;
    }

    @Override
    public void connect() {
        String host = plugin.getConfig().getString("database.MySQL.hostname", "localhost");
        int port = Integer.parseInt(plugin.getConfig().getString("database.MySQL.port", "3306"));
        String name = plugin.getConfig().getString("database.MySQL.database", "minecraft");
        String username = plugin.getConfig().getString("database.MySQL.username", "root");
        String password = plugin.getConfig().getString("database.MySQL.password", "password");
        boolean useSSL = plugin.getConfig().getBoolean("database.MySQL.useSSL", false);

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + host + ":" + port + "/" + name + "?useSSL=" + useSSL + "&characterEncoding=utf8",
                    username,
                    password
            );
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