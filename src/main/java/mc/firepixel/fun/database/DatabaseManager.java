
package mc.firepixel.fun.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import mc.firepixel.fun.Firepixel;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseManager {

    private final Firepixel plugin;
    private HikariDataSource dataSource;
    private SqliteSupport sqliteSupport;

    public DatabaseManager(Firepixel plugin) {
        this.plugin = plugin;
    }

    public void connect() throws Exception {

        String type = plugin.getConfig()
                .getString("database.type", "sqlite")
                .toLowerCase();

        HikariConfig config = new HikariConfig();

        if (type.equals("sqlite")) {

            File file = new File(
                    plugin.getDataFolder(), "database.db"
            );

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            sqliteSupport = SqliteSupport.create(
                    "jdbc:sqlite:" + file.getAbsolutePath()
            );

            config.setDataSource(sqliteSupport.getDataSource());
            config.setMaximumPoolSize(1);
            config.setPoolName("Firepixel-SQLite");

        } else if (type.equals("mysql")) {

            String host = plugin.getConfig().getString(
                    "database.mysql.host", "localhost"
            );

            int port = plugin.getConfig().getInt(
                    "database.mysql.port", 3306
            );

            String database = plugin.getConfig().getString(
                    "database.mysql.database", "firepixel"
            );

            String username = plugin.getConfig().getString(
                    "database.mysql.username", "root"
            );

            String password = plugin.getConfig().getString(
                    "database.mysql.password", ""
            );

            config.setJdbcUrl(
                    "jdbc:mysql://" + host + ":" + port + "/" + database
                            + "?useSSL=false&serverTimezone=UTC"
            );

            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("Firepixel-MySQL");

        } else {
            throw new IllegalArgumentException(
                    "Invalid database type: " + type
                            + ". Use sqlite or mysql."
            );
        }

        config.setConnectionTimeout(10000);

        dataSource = new HikariDataSource(config);

        plugin.getLogger().info(
                "Connected to " + type.toUpperCase() + " database!"
        );
    }

    public void createTables() throws Exception {

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS player_data (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "name VARCHAR(16) NOT NULL, " +
                            "first_join BIGINT NOT NULL, " +
                            "last_join BIGINT NOT NULL, " +
                            "time_played BIGINT NOT NULL DEFAULT 0" +
                            ")"
            );

            statement.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS server_stats (" +
                            "id INTEGER PRIMARY KEY, " +
                            "total_players BIGINT NOT NULL DEFAULT 0" +
                            ")"
            );

            try (PreparedStatement check = connection.prepareStatement(
                    "SELECT id FROM server_stats WHERE id = 1"
            );
                 ResultSet result = check.executeQuery()) {

                if (!result.next()) {
                    try (PreparedStatement insert =
                                 connection.prepareStatement(
                                         "INSERT INTO server_stats " +
                                                 "(id, total_players) VALUES (1, 0)"
                                 )) {
                        insert.executeUpdate();
                    }
                }
            }
        }

        plugin.getLogger().info("Database tables initialized!");
    }

    public Connection getConnection() throws Exception {

        if (dataSource == null || dataSource.isClosed()) {
            throw new IllegalStateException(
                    "Database connection is not available!"
            );
        }

        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }

        if (sqliteSupport != null) {
            sqliteSupport.close();
            sqliteSupport = null;
        }
    }
}