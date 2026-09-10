# Firepixel

A Hypixel-style Minecraft plugin network built on the Spigot 1.8.8 API.

## Requirements

- Java 8
- Spigot 1.8.8
- Maven

## Build

```
mvn clean package
```

The output jar is generated in `target/Firepixel-1.0-SNAPSHOT.jar`.

## Installation

1. Drop `Firepixel-1.0-SNAPSHOT.jar` into your server `plugins` folder.
2. Start the server once to generate `plugins/Firepixel/config.yml`.
3. Edit `config.yml` to choose SQLite or MySQL.
4. Restart the server.

## Database

The plugin supports two storage backends. They are selected from `config.yml`:

```yaml
database:
  Storage: sqlite
  MySQL:
    hostname: localhost
    username: root
    database: minecraft
    port: '3306'
    password: password
    useSSL: false
  table: firepixel.db
```

- `Storage: sqlite` uses a local file named by `table` (`firepixel.db`).
- `Storage: mysql` connects to the MySQL server using the `MySQL` block.
- Player records are stored in the `firepixel_players` SQL table.

Every player row stores:

| Column     | Type        | Description                  |
|------------|-------------|------------------------------|
| uuid       | VARCHAR(36) | Player UUID (primary key)    |
| name       | VARCHAR(16) | Last known player name       |
| first_join | BIGINT      | First join timestamp         |
| last_join  | BIGINT      | Most recent join timestamp   |

## How it works

On join, the player is loaded from the database. On quit, their record is saved.

```java
public class PlayerJoinListener implements Listener {

    private final Firepixel plugin;

    public PlayerJoinListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().load(event.getPlayer().getUniqueId(), event.getPlayer().getName());
    }
}
```

```java
public class PlayerQuitListener implements Listener {

    private final Firepixel plugin;

    public PlayerQuitListener(Firepixel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().save(event.getPlayer().getUniqueId());
    }
}
```

The storage backend is chosen at startup by `DatabaseManager`:

```java
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
```

Player data is read and written through `PlayerDataManager`:

```java
public PlayerData load(UUID uuid, String name) {
    ...
}
```

## Project structure

```
src/main/java/net/firepixel/fun/
  Firepixel.java              Main plugin class
  database/                   SQLite and MySQL storage
  player/                     Player data model and manager
  listener/                   Join and quit listeners
src/main/resources/
  config.yml                  Plugin configuration
  plugin.yml                  Plugin descriptor
```
