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
3. Edit `config.yml` to choose SQLite or MySQL, set the spawn, language and report settings.
4. Restart the server.

## Commands

| Command                        | Description                     | Permission                         |
|--------------------------------|---------------------------------|------------------------------------|
| `/setspawn`                    | Set the spawn to your position  | `firepixel.setspawn`               |
| `/spawn`                       | Teleport to the spawn           | -                                  |
| `/language`                    | Open the language menu          | -                                  |
| `/language <code>`             | Change your own language        | -                                  |
| `/setlanguage <code> [player]` | Set a player's language         | `firepixel.setlanguage`            |
| `/report` (`/creport`)         | Open the report menu            | -                                  |
| `/reportaccept <reporter>`     | Accept a pending report         | `firepixel.report.staff`           |
| `/ban`, `/tempban`             | Ban a player                    | `firepixel.punishment.ban`         |
| `/mute`, `/tempmute`           | Mute a player                   | `firepixel.punishment.mute`        |
| `/warn`, `/kick`               | Warn or kick a player           | `firepixel.punishment.warn` / `kick` |
| `/unban`, `/unmute`            | Remove a punishment             | `firepixel.punishment.unban` / `unmute` |

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
- Player records are stored in `firepixel_players`, reports in `firepixel_reports` and punishments in `firepixel_punishments`.

Every player row stores:

| Column     | Type        | Description                  |
|------------|-------------|------------------------------|
| uuid       | VARCHAR(36) | Player UUID (primary key)    |
| name       | VARCHAR(16) | Last known player name       |
| first_join | BIGINT      | First join timestamp         |
| last_join  | BIGINT      | Most recent join timestamp   |
| language   | VARCHAR(8)  | Selected language code       |

`DatabaseManager` creates the module tables on startup:

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
    createModuleTables(type);
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
    items:
      en:
        name: '&aEnglish'
        texture: 'eyJ0ZXh0dXJlcyI6...'
        lore:
        - '&7Change your language to English.'
  changed: '&aYou set your language to &6%language%&a!'
  invalid: '&cUnknown language.'
  world-disabled: '&cYou cannot change your language in this world.'
```

Each language entry uses a player head with a custom texture. The texture falls back to a built-in map when not set in the file.

`/language` opens a 54-slot menu with player heads, a help book in slot 50 and a close barrier in slot 49:

```java
public void open(Player viewer) {
    LanguageMenuHolder holder = new LanguageMenuHolder();
    Inventory inventory = Bukkit.createInventory(holder, 54, title);
    inventory.setItem(49, buildCloseItem(viewerLanguage));
    inventory.setItem(50, buildHelpItem(viewerLanguage));

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

## Report

`/report` (alias `/creport`) opens the reason menu. Selecting a reason shows the online players as heads, and selecting a player opens the confirm menu. Reasons, items and lore are all defined in the language files:

```yaml
report:
  menu:
    reason-title: '&8Report'
    player-title: '&8Select a Player'
    confirm-title: '&8Confirm Report'
    reasons:
      chat_abuse:
        slot: 20
        material: BOOK_AND_QUILL
        data: 0
        name: '&aChat Abuse'
        lore:
        - '&7Report players for abusive chat.'
        - '&7'
        - '&eClick to Select!'
```

Submitting a report saves it and notifies staff:

```java
public void submit(Player reporter, String target, String reasonKey) {
    PreparedStatement insert = connection.prepareStatement("INSERT INTO firepixel_reports (reporter, reported, reason, timestamp, status) VALUES (?, ?, ?, ?, 'pending')");
    ...
    for (Player staff : Bukkit.getOnlinePlayers()) {
        if (staff.hasPermission(plugin.getConfig().getString("report.staff-permission", "firepixel.report.staff"))) {
            staff.sendMessage(notify);
        }
    }
}
```

## Punishments

`PunishmentManager` stores bans, mutes, warns and kicks and applies them immediately:

```java
public boolean addPunishment(String playerName, PunishmentType type, String reason, String operator, long duration) {
    PreparedStatement insert = connection.prepareStatement("INSERT INTO firepixel_punishments (player_name, punishment_type, reason, operator, duration, start_time, end_time, active) VALUES (?, ?, ?, ?, ?, ?, ?, 1)");
    ...
}
```

Banned players are blocked at login and muted players have their chat cancelled:

```java
@EventHandler
public void onLogin(PlayerLoginEvent event) {
    if (!plugin.getPunishmentManager().isPlayerBanned(event.getPlayer().getName())) {
        return;
    }

    event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
    event.setKickMessage(plugin.getPunishmentManager().buildScreen(language, "ban", "Banned", null));
}
```

Screens and broadcast messages are defined in the language files:

```yaml
punishments:
  banned: '&c%player% was successfully banned!'
  banned-broadcast: '&c&l&n%player% &cgot banned by &l%staff% &cFor %reason% permanently'
  screens:
    ban: '&cYou are permanently banned from this server!\n&7\n&7Reason: &f%reason%\n&7Find out more: &b&n%appeal%'
```

## Join Messages

The default join and quit messages are removed. A ranked join message is broadcast instead, configured in the language files:

```yaml
join-message:
  hover_text:
  - '%rank%%player%'
  - '&7Firepixel Level: &30'
  ranks:
    vip:
      permission: joinmessages.vip
      message: '&a[VIP] %player% &6slid in the lobby!'
```

`JoinMessageManager` picks the highest matching rank and builds a clickable, hoverable message:

```java
public void handleJoin(Player player) {
    if (!isEnabled() || player == null) {
        return;
    }

    String message = getRankMessage(player, language);
    TextComponent base = new TextComponent("");

    for (BaseComponent component : TextComponent.fromLegacyText(resolve(player, message))) {
        base.addExtra(component);
    }

    base.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, click));

    for (Player recipient : plugin.getServer().getOnlinePlayers()) {
        recipient.spigot().sendMessage(base);
    }
}
```

## Dependencies

- `spigot-api` 1.8.8
- `bungeecord-chat` for clickable and hoverable join messages
- `mysql-connector-j` and `sqlite-jdbc`
- `authlib` (provided) for player head textures
- `LuckPerms` (soft) for rank tags

## Project structure

```
src/main/java/net/firepixel/fun/
  Firepixel.java              Main plugin class
  command/                    setspawn, spawn, language, setlanguage, report, punishments
  database/                   SQLite and MySQL storage
  player/                     Player data model
  report/                     Report menu holder
  listener/                   Join, quit, language menu, report menu, punishment listeners
  manager/                    Database, player data, spawn, language, menu, report, punishment, join message managers
  menu/                       Language menu holder
  util/                       Material and skull texture utilities
src/main/resources/
  config.yml                  Plugin configuration
  plugin.yml                  Plugin descriptor
  languages/                  Language files
```