package net.firepixel.fun.manager;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.database.Database;
import net.firepixel.fun.player.PlayerData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Firepixel plugin;
    private final Database database;
    private final String table;
    private final Map<UUID, PlayerData> cache = new HashMap<UUID, PlayerData>();

    public PlayerDataManager(Firepixel plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
        this.table = "firepixel_players";
    }

    public PlayerData load(UUID uuid, String name) {
        long now = System.currentTimeMillis();
        PlayerData data;

        try {
            Connection connection = database.getConnection();

            PreparedStatement select = connection.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?");
            select.setString(1, uuid.toString());
            ResultSet result = select.executeQuery();

            if (result.next()) {
                data = new PlayerData(uuid, name, result.getLong("first_join"), now, result.getString("language"));

                PreparedStatement update = connection.prepareStatement("UPDATE " + table + " SET name = ?, last_join = ? WHERE uuid = ?");
                update.setString(1, name);
                update.setLong(2, now);
                update.setString(3, uuid.toString());
                update.executeUpdate();
                update.close();
            } else {
                data = new PlayerData(uuid, name, now, now, plugin.getLanguageManager().getDefaultLanguage());

                PreparedStatement insert = connection.prepareStatement("INSERT INTO " + table + " (uuid, name, first_join, last_join, language) VALUES (?, ?, ?, ?, ?)");
                insert.setString(1, uuid.toString());
                insert.setString(2, name);
                insert.setLong(3, now);
                insert.setLong(4, now);
                insert.setString(5, data.getLanguage());
                insert.executeUpdate();
                insert.close();
            }

            result.close();
            select.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }

        cache.put(uuid, data);
        return data;
    }

    public void save(UUID uuid) {
        PlayerData data = cache.get(uuid);

        if (data == null) {
            return;
        }

        long now = System.currentTimeMillis();
        data.setLastJoin(now);

        try {
            Connection connection = database.getConnection();

            PreparedStatement update = connection.prepareStatement("UPDATE " + table + " SET name = ?, last_join = ?, language = ? WHERE uuid = ?");
            update.setString(1, data.getName());
            update.setLong(2, now);
            update.setString(3, data.getLanguage());
            update.setString(4, uuid.toString());
            update.executeUpdate();
            update.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }

        cache.remove(uuid);
    }

    public PlayerData get(UUID uuid) {
        return cache.get(uuid);
    }

    public String getLanguage(UUID uuid) {
        PlayerData data = cache.get(uuid);

        if (data == null || data.getLanguage() == null) {
            return plugin.getLanguageManager().getDefaultLanguage();
        }

        return data.getLanguage();
    }

    public void setLanguage(UUID uuid, String language) {
        PlayerData data = cache.get(uuid);

        if (data == null) {
            return;
        }

        data.setLanguage(language);

        try {
            Connection connection = database.getConnection();

            PreparedStatement update = connection.prepareStatement("UPDATE " + table + " SET language = ? WHERE uuid = ?");
            update.setString(1, language);
            update.setString(2, uuid.toString());
            update.executeUpdate();
            update.close();
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}