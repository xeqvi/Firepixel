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
3. Edit `config.yml` to choose SQLite or MySQL and set the spawn.
4. Restart the server.

## Commands

| Command    | Description                       | Permission           |
|------------|-----------------------------------|----------------------|
| `/setspawn`| Set the spawn to your position    | `firepixel.setspawn` |
| `/spawn`   | Teleport to the spawn             | -                    |

Commands defined under `spawn.commands` in `config.yml` are registered automatically:

```yaml
spawn:
  world: world
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0
  commands:
    hub: /server lobby
    lobby: /server lobby
    stuck: /spawn
```

- `hub` runs `/server lobby`.
- `lobby` runs `/server lobby`.
- `stuck` runs `/spawn`.

Each of them sends the player an empty message.

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

Player data is loaded and saved through `PlayerDataManager`:

```java
public PlayerData load(UUID uuid, String name) {
    ...
}

public void save(UUID uuid) {
    ...
}
```

## Spawn

On join, players are loaded from the database and teleported to the spawn, then receive an empty message.

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    plugin.getPlayerDataManager().load(event.getPlayer().getUniqueId(), event.getPlayer().getName());

    Location spawn = plugin.getSpawnManager().getSpawn();

    if (spawn != null) {
        event.getPlayer().teleport(spawn);
    }

    event.getPlayer().sendMessage("");
}
```

The spawn is read from and written to `config.yml` by `SpawnManager`:

```java
public Location getSpawn() {
    String worldName = plugin.getConfig().getString("spawn.world");
    World world = Bukkit.getWorld(worldName);

    double x = plugin.getConfig().getDouble("spawn.x");
    double y = plugin.getConfig().getDouble("spawn.y");
    double z = plugin.getConfig().getDouble("spawn.z");
    float yaw = (float) plugin.getConfig().getDouble("spawn.yaw");
    float pitch = (float) plugin.getConfig().getDouble("spawn.pitch");

    return new Location(world, x, y, z, yaw, pitch);
}

public void setSpawn(Location location) {
    plugin.getConfig().set("spawn.world", location.getWorld().getName());
    plugin.getConfig().set("spawn.x", location.getX());
    plugin.getConfig().set("spawn.y", location.getY());
    plugin.getConfig().set("spawn.z", location.getZ());
    plugin.getConfig().set("spawn.yaw", (double) location.getYaw());
    plugin.getConfig().set("spawn.pitch", (double) location.getPitch());
    plugin.saveConfig();
}
```

`/setspawn` stores your current location:

```java
Player player = (Player) sender;
plugin.getSpawnManager().setSpawn(player.getLocation());
```

`/spawn` teleports you back:

```java
Location spawn = plugin.getSpawnManager().getSpawn();

if (spawn != null) {
    player.teleport(spawn);
}

player.sendMessage("");
```

Custom commands from `spawn.commands` are registered at startup and forward to their action:

```java
ConfigurationSection section = getConfig().getConfigurationSection("spawn.commands");

for (String name : section.getKeys(false)) {
    String action = section.getString(name);
    commandMap.register(getName().toLowerCase(), new DynamicCommand(name, action));
}
```

```java
public boolean execute(CommandSender sender, String label, String[] args) {
    Player player = (Player) sender;
    player.performCommand(action.startsWith("/") ? action.substring(1) : action);
    player.sendMessage("");
    return true;
}
```

## Project structure

```
src/main/java/net/firepixel/fun/
  Firepixel.java              Main plugin class
  command/                    setspawn, spawn, dynamic commands
  database/                   SQLite and MySQL storage
  player/                     Player data model and manager
  listener/                   Join and quit listeners
  spawn/                      Spawn manager
src/main/resources/
  config.yml                  Plugin configuration
  plugin.yml                  Plugin descriptor
```