
package mc.firepixel.fun.managers;

import mc.firepixel.fun.Firepixel;
import mc.firepixel.fun.database.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerDataManager {

    private final Firepixel plugin;
    private final DatabaseManager database;

    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();

    private final Map<UUID, Long> sessions =
            new ConcurrentHashMap<UUID, Long>();

    private BukkitTask autoSaveTask;

    public PlayerDataManager(
            Firepixel plugin,
            DatabaseManager database
    ) {
        this.plugin = plugin;
        this.database = database;
    }

    public void handleJoin(Player player) {

        final UUID uuid = player.getUniqueId();
        final String name = player.getName();
        final long now = System.currentTimeMillis();

        sessions.put(uuid, now);

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try (Connection connection = database.getConnection()) {

                    connection.setAutoCommit(false);

                    try {
                        int updated;

                        try (PreparedStatement statement =
                                     connection.prepareStatement(
                                             "UPDATE player_data " +
                                                     "SET name = ?, last_join = ? " +
                                                     "WHERE uuid = ?"
                                     )) {

                            statement.setString(1, name);
                            statement.setLong(2, now);
                            statement.setString(3, uuid.toString());

                            updated = statement.executeUpdate();
                        }

                        if (updated == 0) {

                            try (PreparedStatement statement =
                                         connection.prepareStatement(
                                                 "INSERT INTO player_data " +
                                                         "(uuid, name, first_join, " +
                                                         "last_join, time_played) " +
                                                         "VALUES (?, ?, ?, ?, 0)"
                                         )) {

                                statement.setString(1, uuid.toString());
                                statement.setString(2, name);
                                statement.setLong(3, now);
                                statement.setLong(4, now);

                                statement.executeUpdate();
                            }

                            try (PreparedStatement statement =
                                         connection.prepareStatement(
                                                 "UPDATE server_stats " +
                                                         "SET total_players = " +
                                                         "total_players + 1 " +
                                                         "WHERE id = 1"
                                         )) {

                                statement.executeUpdate();
                            }
                        }

                        connection.commit();

                    } catch (Exception exception) {
                        connection.rollback();
                        throw exception;
                    } finally {
                        connection.setAutoCommit(true);
                    }

                } catch (Exception exception) {
                    plugin.getLogger().severe(
                            "Failed to save player data for " + name
                    );
                    exception.printStackTrace();
                }
            }
        });
    }

    public void handleQuit(Player player) {

        UUID uuid = player.getUniqueId();

        Long start = sessions.remove(uuid);

        if (start == null) {
            return;
        }

        long played = Math.max(
                0L, System.currentTimeMillis() - start
        );

        savePlaytime(uuid, played);
    }

    private void savePlaytime(final UUID uuid, final long played) {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                try (Connection connection = database.getConnection();
                     PreparedStatement statement =
                             connection.prepareStatement(
                                     "UPDATE player_data " +
                                             "SET time_played = time_played + ? " +
                                             "WHERE uuid = ?"
                             )) {

                    statement.setLong(1, played);
                    statement.setString(2, uuid.toString());

                    statement.executeUpdate();

                } catch (Exception exception) {
                    plugin.getLogger().severe(
                            "Failed to save playtime for " + uuid
                    );
                    exception.printStackTrace();
                }
            }
        });
    }

    public void startAutoSave() {

        autoSaveTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                new Runnable() {
                    @Override
                    public void run() {

                        long now = System.currentTimeMillis();

                        for (Map.Entry<UUID, Long> entry :
                                sessions.entrySet()) {

                            UUID uuid = entry.getKey();
                            Long previous = entry.getValue();

                            if (sessions.replace(uuid, previous, now)) {

                                long played = Math.max(
                                        0L, now - previous
                                );

                                savePlaytime(uuid, played);
                            }
                        }
                    }
                },
                1200L,
                1200L
        );
    }

    public void getTotalPlayers(
            final java.util.function.LongConsumer callback
    ) {

        executor.execute(new Runnable() {
            @Override
            public void run() {

                long total = 0L;

                try (Connection connection = database.getConnection();
                     PreparedStatement statement =
                             connection.prepareStatement(
                                     "SELECT total_players " +
                                             "FROM server_stats WHERE id = 1"
                             );
                     ResultSet result = statement.executeQuery()) {

                    if (result.next()) {
                        total = result.getLong("total_players");
                    }

                } catch (Exception exception) {
                    plugin.getLogger().severe(
                            "Failed to retrieve total player count!"
                    );
                    exception.printStackTrace();
                }

                final long finalTotal = total;

                Bukkit.getScheduler().runTask(
                        plugin,
                        new Runnable() {
                            @Override
                            public void run() {
                                callback.accept(finalTotal);
                            }
                        }
                );
            }
        });
    }

    public void shutdown() {

        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        long now = System.currentTimeMillis();

        for (Map.Entry<UUID, Long> entry : sessions.entrySet()) {

            UUID uuid = entry.getKey();
            Long previous = entry.getValue();

            if (sessions.remove(uuid, previous)) {
                savePlaytime(
                        uuid,
                        Math.max(0L, now - previous)
                );
            }
        }

        executor.shutdown();

        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                plugin.getLogger().warning(
                        "Database tasks did not finish in time!"
                );
                executor.shutdownNow();
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
