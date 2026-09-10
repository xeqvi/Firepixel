package net.firepixel.fun.player;

import net.firepixel.fun.Firepixel;
import net.firepixel.fun.database.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {

    private final Database database;
    private final String table;
    private final Map<UUID, PlayerData> cache = new HashMap<UUID, PlayerData>();

    public PlayerDataManager(Firepixel plugin, Database database) {
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
                data = new PlayerData(uuid, name, result.getLong("first_join"), now);

                PreparedStatement update = connection.prepareStatement("UPDATE " + table + " SET name = ?, last_join = ? WHERE uuid = ?");
                update.setString(1, name);
                update.setLong(2, now);
                update.setString(3, uuid.toString());
                update.executeUpdate();
                update.close();
            } else {
                data = new PlayerData(uuid, name, now, now);

                PreparedStatement insert = connection.prepareStatement("INSERT INTO " + table + " (uuid, name, first_join, last_join) VALUES (?, ?, ?, ?)");
                insert.setString(1, uuid.toString());
                insert.setString(2, name);
                insert.setLong(3, now);
                insert.setLong(4, now);
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

            PreparedStatement update = connection.prepareStatement("UPDATE " + table + " SET name = ?, last_join = ? WHERE uuid = ?");
            update.setString(1, data.getName());
            update.setLong(2, now);
            update.setString(3, uuid.toString());
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
}