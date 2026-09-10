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
2. Start the server once to generate `plugins/Firepixel/config.yml` and `plugins/Firepixel/languages/`.
3. Edit `config.yml` to choose SQLite or MySQL, set the spawn and the language settings.
4. Restart the server.

## Commands

| Command     | Description                    | Permission           |
|-------------|--------------------------------|----------------------|
| `/setspawn` | Set the spawn to your position | `firepixel.setspawn` |
| `/spawn`    | Teleport to the spawn          | -                    |
| `/language` | Open the language menu         | -                    |

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
| language   | VARCHAR(8)  | Selected language code       |

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

The spawn section controls where players are teleported:

```yaml
spawn:
  world: world
  x: 0.0
  y: 64.0
  z: 0.0
  yaw: 0.0
  pitch: 0.0
  enabled_spawn_worlds:
  - world
  commands:
    hub: /server lobby
    lobby: /server lobby
    stuck: /spawn
```

- `enabled_spawn_worlds` lists the worlds where joining players are sent to spawn.
- Commands under `spawn.commands` are registered automatically and forward to their action.

On join, players are loaded from the database and teleported to spawn if their world is enabled:

```java
@EventHandler
public void onJoin(PlayerJoinEvent event) {
    plugin.getPlayerDataManager().load(event.getPlayer().getUniqueId(), event.getPlayer().getName());

    if (!plugin.getSpawnManager().isEnabledForWorld(event.getPlayer().getWorld().getName())) {
        return;
    }

    Location spawn = plugin.getSpawnManager().getSpawn();

    if (spawn != null) {
        event.getPlayer().teleport(spawn);
    }
}
```

`/setspawn` checks the permission and sends a language message:

```java
if (!player.hasPermission("firepixel.setspawn")) {
    player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.no-permission"));
    return true;
}

plugin.getSpawnManager().setSpawn(player.getLocation());
player.sendMessage(plugin.getLanguageManager().getMessage(language, "spawn.set"));
```

## Languages

Language files live in `plugins/Firepixel/languages/messages_<code>.yml`. Each file starts with its display name:

```yaml
language_name: English
language:
  menu:
    title: '&8Select Language'
    help:
      name: '&aHelp us Translate Firepixel'
      lore:
      - '&7We have added a way for you to help us translate Firepixel.'
    close:
      name: '&cClose'
      lore:
      - '&7Close the menu.'
    items:
      en:
        name: '&aEnglish'
        texture: 'eyJ0ZXh0dXJlcyI6...'
        lore:
        - '&7Change your language to English.'
        - '&7Click to choose this language.'
  changed: '&aYou set your language to &6%language%&a!'
  world-disabled: '&cYou cannot change your language in this world.'
spawn:
  set: '&aYou set the spawn location!'
  no-permission: '&cYou are not allowed to do this!'
  teleported: '&aTeleported to spawn!'
  not-set: '&cSpawn is not set!'
  disabled-world: '&cSpawn is disabled in this world!'
```

Each language entry uses a player head with a custom texture. The texture falls back to a built-in map when not set in the file.

The language section in `config.yml`:

```yaml
language:
  default-language: en
  disabled_language_worlds:
  - test
```

- `disabled_language_worlds` lists worlds where the language menu cannot be opened.
- Every supported language file is detected automatically from the `languages` folder.

`LanguageManager` loads the files and returns colored messages and lists:

```java
public void reload() {
    languages.clear();
    File folder = new File(plugin.getDataFolder(), "languages");
    plugin.saveResource("languages/messages_en.yml", false);
    ...
}

public String getMessage(String language, String path) {
    YamlConfiguration config = languages.get(resolveLanguage(language));
    String value = config.getString(path);
    return ChatColor.translateAlternateColorCodes('&', value);
}
```

`/language` opens a 54-slot menu with player heads, a help book and a close barrier:

```java
public void open(Player viewer) {
    LanguageMenuHolder holder = new LanguageMenuHolder();
    Inventory inventory = Bukkit.createInventory(holder, 54, title);
    inventory.setItem(49, buildCloseItem(viewerLanguage));
    inventory.setItem(51, buildHelpItem(viewerLanguage));

    for (int i = 0; i < LANGUAGE_SLOTS.length; i++) {
        String code = supported.get(i);
        inventory.setItem(LANGUAGE_SLOTS[i], createLanguageHead(viewerLanguage, code));
        holder.setLanguage(LANGUAGE_SLOTS[i], code);
    }
}
```

Heads are built through `MaterialUtil`, which applies a base64 texture via `authlib`:

```java
ItemStack item = MaterialUtil.createSkull();
SkullMeta meta = (SkullMeta) item.getItemMeta();
MaterialUtil.applyTexture(meta, texture);
```

Clicking a head saves the language, plays a sound and shows a confirmation:

```java
plugin.getPlayerDataManager().setLanguage(player.getUniqueId(), code);
player.playSound(player.getLocation(), Sound.valueOf("NOTE_PLING"), 1.0f, 1.0f);
player.sendMessage(plugin.getLanguageManager().getMessage(code, "language.changed"));
```

## Dependencies

- `spigot-api` 1.8.8
- `bungeecord-chat`
- `mysql-connector-j` and `sqlite-jdbc`
- `authlib` (provided) for player head textures

## Project structure

```
src/main/java/net/firepixel/fun/
  Firepixel.java              Main plugin class
  command/                    setspawn, spawn, language, dynamic commands
  database/                   SQLite and MySQL storage
  player/                     Player data model
  listener/                   Join, quit and language menu listeners
  manager/                    Database, player data, spawn, language, menu managers
  menu/                       Language menu holder
  util/                       Material and skull texture utilities
src/main/resources/
  config.yml                  Plugin configuration
  plugin.yml                  Plugin descriptor
  languages/                  Language files
```