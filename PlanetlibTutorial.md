# PlanetLib Tutorial

A practical guide to using PlanetLib for Bukkit/Paper plugin development, based on real-world usage patterns.

---

## Table of Contents

1. [Installation](#1-installation)
2. [Initialization](#2-initialization)
3. [Configuration with Config](#3-configuration-with-config)
4. [Internationalization with Lang](#4-internationalization-with-lang)
5. [Message Themes](#5-message-themes)
6. [The Message System](#6-the-message-system)
7. [Wrapper Classes](#7-wrapper-classes)
8. [Command System](#8-command-system)
9. [Database System](#9-database-system)
10. [Scheduler (FoliaLib)](#10-scheduler-folialib)
11. [Console Logging](#11-console-logging)
12. [Hooks](#12-hooks)
13. [Putting It All Together](#13-putting-it-all-together)

---

## 1. Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
    mavenLocal()
}

dependencies {
    implementation("com.github.jason31416:planetlib:1.4.2")
}
```

### Shadow JAR Relocation

If you use the Shadow plugin, relocate PlanetLib to avoid conflicts:

```kotlin
tasks.shadowJar {
    relocate("cn.jason31416.planetlib", "com.yourplugin.lib.planetlib")
}
```

---

## 2. Initialization

Call `PlanetLib.initialize()` in your plugin's `onEnable()`:

```java
public final class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        PlanetLib.initialize(this, Required.VAULT, Required.NBT, Required.PLACEHOLDERAPI);
    }
}
```

The `Required` enum specifies optional dependencies to hook into:

| Value | What it does                                                                          |
|---|---------------------------------------------------------------------------------------|
| `Required.VAULT` | Enables economy integration (`SimplePlayer.getBalance()`, etc.)                       |
| `Required.NBT` | Preloads the NBT API (For any GUI-related stuff, this is nessecery)                   |
| `Required.PLACEHOLDERAPI` | Enables PlaceholderAPI expansion in messages (will process external PAPI in messages) |

All are optional. Call `PlanetLib.initialize(this)` with no arguments if you don't need any hooks.

After initialization, `PlanetLib.getScheduler()` returns a FoliaLib `PlatformScheduler` for cross-platform task scheduling.

---

## 3. Configuration with Config

PlanetLib provides a simple YAML config wrapper that auto-loads `config.yml` from your plugin's data folder.

### Loading

```java
Config.load(this); // Call once in onEnable()
```

This saves `config.yml` from your jar resources to disk (if it doesn't exist), then loads it. If the file's `version` key differs from the jar's, new keys are automatically merged while preserving user edits and comments.

### Reading Values

```java
String lang = Config.getString("lang", "en-us");    // with default
int maxClaims = Config.getInt("claim.max-claims");   // dot-separated path
double price = Config.getDouble("claim.price-per-block");
boolean enabled = Config.getBoolean("feature.enabled");
String value = Config.getString("some.key");          // returns "" if missing
boolean exists = Config.contains("some.key");
```

### Accessing Sections

```java
MapTree groups = Config.getSection("claim.groups");
for (String key : groups.getKeys()) {
    String name = groups.getString(key + ".name");
    int weight = groups.getInt(key + ".weight");
}
```

### Config File Format

```yaml
# config.yml
lang: "en-us"
theme: "default"

claim:
  name-regex: "[A-Za-z0-9_-]{1,64}"
  max-claims-per-player: -1
  price-per-block: 1.0

# Required for auto-merge feature
version: "1.0"
```

**Convention:** Include a `version` key. When you ship a new version with new config keys, bump the version string. PlanetLib will auto-merge new keys into existing user files.

---

## 4. Internationalization with Lang

### Setup

Place language files in `src/main/resources/lang/`:

```
src/main/resources/
  lang/
    en-us.yml
    zh-cn.yml
    theme.yml
```

Initialize in `onEnable()` (or a reload method):

```java
Util.saveFolder("lang");  // Extracts the lang/ folder from jar to disk
Lang.init("lang/" + Config.getString("lang", "en-us") + ".yml");
```

`Lang.init()` does two things:
1. Loads the specified YAML file as the primary message source.
2. Loads the same file from the jar as a fallback (so missing keys don't break).

### Language File Format

```yaml
# lang/en-us.yml
command:
  player-only: "<error-primary>Error: <error-content>This command can only be run by a player"
  not-in-claim: "<error-primary>Error: <error-content>You are not in a claim"
  create-success: "<content>Created claim <highlight>%claim%<content> for <highlight>%price%"
  help-message:
    - "<dark_gray>=== <highlight>MyPlugin <muted>Help <dark_gray>==="
    - "<content>Page <highlight>%page%<content>/<highlight>%page-count%"
```

**Key points:**
- Use dot-separated keys for nesting: `command.player-only` maps to `command: { player-only: "..." }`.
- Single strings become `Message`, YAML lists become `MessageList`.
- Placeholders use `%key%` syntax.
- Color/style tags use MiniMessage syntax: `<red>`, `<bold>`, `<hover:show_text:'text'>`.

### Reading Messages

```java
// Single-line message
Message msg = Lang.getMessage("command.player-only");

// With explicit default
Message msg = Lang.getMessage("some.key", "Default text");

// Multi-line message
MessageList list = Lang.getMessageList("command.help-message");

// Raw string (for building formatted output)
String raw = Lang.messageLoader.getRawMessage("some.key", "fallback");
```

**Fallback behavior:** If a key is missing from the user's lang file, PlanetLib checks the jar-bundled version. If still missing, it returns the key name in red (`&ckey.name`).

---

## 5. Message Themes

Themes let you define color variables that are applied to all messages globally.

### Theme File

```yaml
# lang/theme.yml
default:
  content: "<#2ECCF0>"
  highlight: "<#9BE66A>"
  muted: "<#9a9b9d>"
  error-primary: "<#CC1613>"
  error-content: "<#F85654>"
  error-highlight: "<#F8D854>"

dark:
  content: "<#AAAAAA>"
  highlight: "<#FFFFFF>"
  # ...
```

### Loading

```java
MessageTheme.loadThemesFromFile("lang/theme.yml");
MessageTheme.useTheme(Config.getString("theme", "default"));
```

### Usage in Messages

Theme tags are replaced at render time. In your lang YAML:

```yaml
command:
  player-only: "<error-primary>Error: <error-content>Player only command"
  success: "<content>Hello <highlight>world"
```

When rendered with the `default` theme above, `<content>` becomes `<#2ECCF0>`, `<highlight>` becomes `<#9BE66A>`, etc.

**Convention:** Define semantic color roles (`content`, `highlight`, `error-*`, `muted`) rather than literal colors. This lets users customize the look by editing only `theme.yml`.

---

## 6. The Message System

### Overview

All user-facing text flows through the `Message` interface. There are two concrete types:

| Type | When to use | Created from |
|---|---|---|
| `StringMessage` | Single-line messages | `Lang.getMessage(key)` or `Message.of(string)` |
| `MessageList` | Multi-line messages | `Lang.getMessageList(key)` or `Message.of(list)` |

### Creating Messages

```java
// From lang file (preferred). Works for both list and string.
Message msg = Lang.getMessage("command.success");

// From raw string (Use lang file instead unless necessary)
Message msg = Message.of("<green>Hello!");

// From list (Use lang file instead unless necessary)
MessageList list = Message.of(List.of("line1", "line2"));
```

### Placeholders

```java
Message msg = Lang.getMessage("command.create-success")
    .add("claim", claimName)         // replaces %claim%
    .add("price", 100);           // replaces %price%
```

### Sending Messages

```java
// To a player (chat)
msg.send(player);

// To a SimplePlayer
msg.send(simplePlayer);

// To a CommandSender
msg.send(sender);

// Action bar (transient, above hotbar)
msg.sendActionbar(player);
msg.sendActionbar(simplePlayer);

// To all online players + console
msg.broadcast();

// To a collection of players
msg.send(List.of(player1, player2));
```

**Note:** `sendActionbar()` only works on `StringMessage`. Calling it on a `MessageList` throws `UnsupportedOperationException`.

### Rendering

```java
// Legacy section-sign format (for places that need plain strings, might lose some formatting)
String legacy = msg.toString();

// MiniMessage serialized format (preserves Minimessage tags, the server cannot directly process it, but when you tries to put this as placeholder in another message, it is preferred)
String formatted = msg.toFormatted();

// Adventure Component (for advanced use)
Component component = msg.toComponent();
```

### Raw Text

When you need to fetch a raw string from the language file:

```java
String raw = Lang.messageLoader.getRawMessage("some.key", "fallback");
```

---

## 7. Wrapper Classes

PlanetLib wraps Bukkit objects with serializable, utility-rich wrappers. Always use these instead of raw Bukkit types in your domain model.

### SimplePlayer

Wraps `OfflinePlayer`. Serializable by UUID.

```java
// Creation
SimplePlayer player = SimplePlayer.of(bukkitPlayer);
SimplePlayer player = SimplePlayer.of(playerUUID);
SimplePlayer player = SimplePlayer.of("PlayerName");
SimplePlayer player = SimplePlayer.of(commandSender);   // returns null if console

// Properties
UUID uuid = player.getUUID();
String name = player.getName();
boolean online = player.isOnline();
Player bukkit = player.getPlayer();  // throws if offline

// Location (online only)
SimpleLocation loc = player.getLocation();

// Economy (requires Required.VAULT)
double balance = player.getBalance();
player.addBalance(100.0);
boolean success = player.withdrawBalance(50.0);

// Teleportation (Folia-safe)
player.teleport(simpleLocation).thenAccept(success -> {
    if (success) { /* teleported */ }
});

// Messaging
player.sendMessage(message);

// Equality: based on UUID
player1.equals(player2);  // true if same UUID
```

### SimpleLocation

Wraps Bukkit `Location`. Immutable, serializable.

```java
// Creation
SimpleLocation loc = SimpleLocation.of(bukkitLocation);
SimpleLocation loc = SimpleLocation.of(bukkitBlock);
SimpleLocation loc = SimpleLocation.of(x, y, z, simpleWorld);

// Properties
double x = loc.getX();
double y = loc.getY();
double z = loc.getZ();
SimpleWorld world = loc.getWorld();

// Block-aligned location (floors coordinates)
SimpleLocation blockLoc = loc.getBlockLocation();

// Bukkit conversions
Location bukkit = loc.getBukkitLocation();
Block block = loc.getBlock();

// Chunk location
SimpleChunkLocation chunk = loc.getChunkLocation();

// Relative offset
SimpleLocation above = loc.getRelative(0, 1, 0);

// Material access
Material mat = loc.getBlockMaterial();
loc.setBlockMaterial(Material.STONE);

// Equality: based on x, y, z, world
```

### SimpleWorld

Wraps Bukkit `World`. Serializable by UUID.

```java
// Creation
SimpleWorld world = SimpleWorld.of(bukkitWorld);
SimpleWorld world = SimpleWorld.of(worldUUID);
SimpleWorld world = SimpleWorld.of("world_name");
SimpleWorld world = SimpleWorld.defaultWorld();

// Properties
World bukkit = world.getBukkitWorld();
String name = world.getName();

// Equality: based on world UUID
```

### SimpleSender

Wraps `CommandSender`. Use for console/player-agnostic code.

```java
SimpleSender sender = SimpleSender.of(commandSender);

boolean isPlayer = sender.isPlayer();
SimplePlayer player = sender.toPlayer();  // throws if not a player
sender.sendMessage(message);
CommandSender bukkit = sender.sender();
```

### When to Use Wrappers

- **Data models** store `SimplePlayer` and `SimpleLocation`, not raw Bukkit types. This ensures serializability and clean API boundaries.
- **Method signatures** accept wrappers: `void doSomething(SimplePlayer player, SimpleLocation loc)`.
- **Conversion** at the boundary: `SimplePlayer.of(event.getPlayer())` at the event handler level, then pass the wrapper deeper.

---

## 8. Command System

PlanetLib provides a hierarchical command framework with automatic tab completion.

### Root Command

Create a class extending `RootCommand`:

```java
public class MyPluginCommand extends RootCommand {
    public MyPluginCommand() {
        super("myplugin");  // Must match plugin.yml command name

        // Register subcommands
        new CreateCommand(this);
        new HelpCommand(this);
    }

    @Override
    public Message execute(ICommandContext context) {
        // Called when no subcommand is specified
        return Lang.getMessage("command.help");
    }
}
```

Register in `onEnable()`:

```java
new MyPluginCommand().register();
```

### Child Command

Create subcommands extending `ChildCommand`:

```java
public class CreateCommand extends ChildCommand {
    public CreateCommand(IParentCommand parent) {
        super("create", parent);  // Registers as /myplugin create
    }

    @Override
    public Message execute(ICommandContext context) {
        // context.args() = args after "create"
        // context.player() = SimplePlayer (null if console)
        // context.sender() = SimpleSender

        if (context.player() == null) {
            return Lang.getMessage("command.player-only");
        }

        if (context.args().isEmpty()) {
            return Lang.getMessage("command.create-usage");
        }

        String name = context.getArg(0);
        // ... do work ...

        return Lang.getMessage("command.create-success")
                .copy()
                .add("name", name);
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        if (context.args().size() == 1) {
            return List.of("option1", "option2").stream()
                .filter(s -> s.startsWith(context.getArg(0)))
                .toList();
        }
        return List.of();
    }
}
```

### ICommandContext API

```java
// Sender info
SimplePlayer player = context.player();     // null if console
SimpleSender sender = context.sender();     // always non-null

// Arguments
List<String> args = context.args();         // remaining args
String arg0 = context.getArg(0);            // by index, "" if out of bounds
int page = context.getIntArg(0);            // parsed int, 0 on failure
double val = context.getDoubleArg(0);       // parsed double, 0 on failure
SimplePlayer target = context.getPlayerArg(0); // resolved player

// Validation
boolean ok = context.checkArgs(ParameterType.STRING, ParameterType.PLAYER);
// Returns false and automatically sends usage message if args don't match

```

### ParameterType

| Type | Parses | Usage placeholder |
|---|---|---|
| `STRING` | Any string | `<String>` |
| `INTEGER` | `Integer.parseInt()` | `<Integer>` |
| `DOUBLE` | `Double.parseDouble()` | `<Number>` |
| `PLAYER` | `SimplePlayer.of(name)` | `<Player>` |

### Command Tree Example

```java
public class MyPluginCommand extends RootCommand {
    public MyPluginCommand() {
        super("myplugin");

        // Subcommands are auto-registered in their constructors
        new CreateCommand(this);
        new RemoveCommand(this);
        new InfoCommand(this);
        new HelpCommand(this);
    }

    @Override
    public Message execute(ICommandContext context) {
        return new HelpCommand(this).execute(context);
    }
}
```

### plugin.yml

```yaml
commands:
  myplugin:
    description: "Main plugin command"
    usage: "/myplugin <subcommand>"
```

---

## 9. Database System

PlanetLib provides a fluent SQL query builder with SQLite and MySQL support.

### Setup

```java
public class DataHandler {
    private static Database database;

    public static void init() {
        File dbFile = new File(plugin.getDataFolder(), "database.db");
        database = Database.createSqlite(dbFile);

        // Define schema
        database.registerTable(new TableSchema("users")
            .addColumn("uuid", new StringColumn().setPrimaryKey(true).setUnique(true))
            .addColumn("name", new StringColumn())
            .addColumn("balance", new DoubleColumn())
            .addColumn("created_at", new LongColumn())
        );

        database.registerTable(new TableSchema("items")
            .addColumn("id", new IntegerColumn().setPrimaryKey(true).setUnique(true))
            .addColumn("user_uuid", new StringColumn())
            .addColumn("item_type", new StringColumn())
            .addColumn("amount", new IntegerColumn())
        );

        // Create tables / add missing columns
        database.initializeSchema();

        // Optional: create indexes via raw SQL
        database.getSqlInstance().execute(
            "CREATE INDEX IF NOT EXISTS idx_items_user ON items(user_uuid)",
            List.of()
        );
    }

    public static Database getDatabase() { return database; }

    public static void close() {
        database.getSqlInstance().close();
    }
}
```

### Column Types

| Class | SQL Type | Java Type |
|---|---|---|
| `StringColumn` | `VARCHAR(255)` | `String` |
| `IntegerColumn` | `INT` | `Integer` |
| `LongColumn` | `BIGINT` | `Long` |
| `DoubleColumn` | `DOUBLE` | `Double` |
| `BooleanColumn` | `BOOLEAN` | `Boolean` |
| `BlobColumn` | `BLOB` | `byte[]` |

All support `.setPrimaryKey(true)` and `.setUnique(true)`.

### Querying

```java
Database db = DataHandler.getDatabase();

// SELECT * FROM users WHERE uuid = ?
Optional<MapTree> user = db.select("users")
    .keyEquals("uuid", playerUUID.toString())
    .one();

// SELECT * FROM items WHERE user_uuid = ? ORDER BY amount DESC
List<MapTree> items = db.select("items")
    .keyEquals("user_uuid", playerUUID.toString())
    .descending("amount")
    .list();

// SELECT name, balance FROM users WHERE balance > ? LIMIT 10
List<MapTree> rich = db.select("users")
    .selectColumn("name")
    .selectColumn("balance")
    .where("balance > ?", 1000.0)
    .limit(10)
    .list();

// Read results
for (MapTree row : items) {
    String type = row.getString("item_type");
    int amount = row.getInt("amount");
}
```

### Inserting

```java
db.insert("users")
    .value("uuid", playerUUID.toString())
    .value("name", playerName)
    .value("balance", 0.0)
    .value("created_at", System.currentTimeMillis())
    .executeUpdate();
```

### Updating

```java
db.update("users")
    .value("balance", newBalance)
    .keyEquals("uuid", playerUUID.toString())
    .executeUpdate();
```

### Deleting

```java
db.delete("items")
    .keyEquals("id", itemId)
    .keyEquals("user_uuid", userUUID)  // AND condition
    .executeUpdate();
```

### Upserting

```java
// INSERT or UPDATE on conflict on primary key
db.upsert("users")
    .value("uuid", playerUUID.toString())
    .value("name", playerName)
    .value("balance", newBalance)
    .executeUpdate();
```

### Batch Operations

```java
db.executeBatch(List.of(
    db.delete("items").keyEquals("user_uuid", uuid),
    db.insert("items").value("user_uuid", uuid).value("item_type", "sword"),
    db.update("users").value("balance", 0.0).keyEquals("uuid", uuid)
));
```

All statements run in a single transaction. If any fails, the entire batch is rolled back.

### Async Operations

Every operation has an async variant:

```java
db.select("users").keyEquals("uuid", uuid).oneAsync()
    .thenAccept(user -> { /* on main or async thread */ });

db.insert("users").value(...).executeUpdateAsync()
    .thenAccept(rowsAffected -> { /* ... */ });

db.executeBatchAsync(statements)
    .thenAccept(results -> { /* ... */ });
```

### Raw SQL

```java
// Via SQLInstance
db.getSqlInstance().execute(
    "CREATE VIRTUAL TABLE IF NOT EXISTS area USING rtree_i32(id, minX, maxX, minY, maxY, minZ, maxZ)",
    List.of()
);

// Via raw statement builder
db.raw("PRAGMA journal_mode=WAL", List.of()).execute();
```

### MapTree as Row

Query results are `MapTree` objects (the same type used for YAML config):

```java
MapTree row = db.select("users").keyEquals("uuid", uuid).one().orElseThrow();

String name = row.getString("name");
int balance = row.getInt("balance");        // auto-converts Double to int
double precise = row.getDouble("balance");
boolean active = row.getBoolean("active");  // defaults to false
```

---

## 10. Scheduler (FoliaLib)

PlanetLib uses FoliaLib for cross-platform scheduling (works on both Folia and Bukkit/Spigot).

```java
PlatformScheduler scheduler = PlanetLib.getScheduler();
```

### Main thread task (Can also be called on async threads to force main thread execution)

```java
scheduler.runNextTick(task -> {
    // Runs after 3 seconds
    doSomething();
});
```

### Delayed Task

```java
scheduler.runLater(task -> {
    // Runs after 3 seconds
    doSomething();
}, 3, TimeUnit.SECONDS);
```

### Repeating Task

```java
scheduler.runTimer(task -> {
    // Runs every 500ms, starting immediately
    refreshDisplay();
}, 0, 500, TimeUnit.MILLISECONDS);
```

### Entity-Safe Task (Folia compatible)

```java
scheduler.runAtEntity(player, task -> {
    // Runs on the player's scheduler (Folia-safe)
    player.sendMessage("Hello!");
});
```

### Location-Safe Task

```java
scheduler.runAtLocation(location, task -> {
    // Runs on the chunk's scheduler
    block.setType(Material.STONE);
});
```

### Async Teleport

```java
scheduler.teleportAsync(player, location).thenAccept(success -> {
    // Teleport completed
});
```

### When to Use What

| Scenario                                                          | Use               |
|-------------------------------------------------------------------|-------------------|
| Delay an action                                                   | `runLater()`      |
| Periodic refresh (display, particles)                             | `runTimer()`      |
| Access entity data safely (Note that player data don't need this) | `runAtEntity()`   |
| Access block/world data safely                                    | `runAtLocation()` |
| Teleport without freezing                                         | `teleportAsync()` |

---

## 11. Console Logging

```java
// Standard logging
PluginLogger.info("Plugin loaded successfully");
PluginLogger.warning("Config key missing, using default");
PluginLogger.error("Failed to connect to database");

// Send a Message (rendered with MiniMessage) to console
PluginLogger.send(Lang.getMessage("console.loading"));
```

`PluginLogger.send()` calls `message.send(Bukkit.getConsoleSender())`, so it renders MiniMessage formatting in the console.

---

## 12. Hooks

### Vault Economy

Requires `Required.VAULT` in initialization.

```java
SimplePlayer player = SimplePlayer.of(bukkitPlayer);

double balance = player.getBalance();
player.addBalance(100.0);
boolean success = player.withdrawBalance(50.0);
```

### PlaceholderAPI

Requires `Required.PLACEHOLDERAPI` in initialization. PAPI placeholders in messages are automatically resolved on every `send()`, `sendActionbar()`, and `toString()`.

```java
// In a lang file:
// welcome: "<content>Welcome %player_name%, you have %vault_eco_balance% coins!"

Lang.getMessage("welcome").send(player);  // PAPI placeholders auto-resolved
```

### Custom Placeholder Handlers

Register a `PlaceholderHandler` to add your own plugin-level placeholder resolution:

```java
InternalPlaceholder.registerPlaceholderHandler((message, player) -> {
    if (player != null) {
        message = message.replace("%player_level%", getLevel(player));
    }
    return message;
});
```

---

## 13. Putting It All Together

### Project Structure

```
src/main/resources/
  config.yml
  plugin.yml
  lang/
    en-us.yml
    zh-cn.yml
    theme.yml

src/main/java/com/yourplugin/
  YourPlugin.java           # Main class, initialization
  command/
    RootCommand.java         # extends RootCommand
    CreateCommand.java       # extends ChildCommand
    HelpCommand.java         # extends ChildCommand
  handler/
    DataHandler.java         # Database setup
    ProtectionListener.java  # Event listeners
  core/
    YourDomainObject.java    # Uses SimplePlayer, SimpleLocation, MapTree
```

### Main Plugin Class

```java
public final class YourPlugin extends JavaPlugin {
    @Getter
    private static YourPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        PlanetLib.initialize(this, Required.VAULT);

        Config.load(this);

        Util.saveFolder("lang");
        Lang.init("lang/" + Config.getString("lang", "en-us") + ".yml");
        MessageTheme.loadThemesFromFile("lang/theme.yml");
        MessageTheme.useTheme(Config.getString("theme", "default"));

        DataHandler.init();

        new RootCommand().register();
        getServer().getPluginManager().registerEvents(new ProtectionListener(), this);

        PluginLogger.send(Lang.getMessage("console.loaded"));
    }

    @Override
    public void onDisable() {
        DataHandler.close();
    }

    public void reloadConfig() {
        Config.load(this);
        Util.saveFolder("lang");
        Lang.init("lang/" + Config.getString("lang", "en-us") + ".yml");
        MessageTheme.loadThemesFromFile("lang/theme.yml");
        MessageTheme.useTheme(Config.getString("theme", "default"));
    }
}
```

### Command Pattern

```java
public class CreateCommand extends ChildCommand {
    public CreateCommand(IParentCommand parent) {
        super("create", parent);
    }

    @Override
    public Message execute(ICommandContext context) {
        // 1. Guard: player-only
        if (context.player() == null) {
            return Lang.getMessage("command.player-only");
        }

        // 2. Validate args
        if (context.args().isEmpty()) {
            return Lang.getMessage("command.create-usage");
        }

        // 3. Business logic
        String name = context.getArg(0);
        // ... validate, create, save ...

        // 4. Return success message
        return Lang.getMessage("command.create-success")
                .add("name", name)
                .add("price", "$100");
    }

    @Override
    public List<String> tabComplete(ICommandContext context) {
        return List.of();
    }
}
```

### Event Listener Pattern

```java
public class MyListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        SimplePlayer player = SimplePlayer.of(event.getPlayer());
        SimpleLocation location = SimpleLocation.of(event.getBlock());
        
        // More logic
    }
}
```

### Domain Model Pattern (This is just for reference, your implementation may vary but this may be an example to consider)

```java
public class MyObject {
    @Getter private final SimplePlayer owner;
    @Getter private final String uuid;
    @Getter private final SimpleLocation location;

    public MyObject(SimplePlayer owner, String uuid, SimpleLocation location) {
        this.owner = owner;
        this.uuid = uuid;
        this.location = location;
    }

    // Use MapTree for reading DB rows
    public static MyObject fromRow(MapTree row) {
        return new MyObject(
            SimplePlayer.of(UUID.fromString(row.getString("owner_uuid"))),
            row.getString("uuid"),
            new SimpleLocation(
                row.getDouble("x"),
                row.getDouble("y"),
                row.getDouble("z"),
                SimpleWorld.of(UUID.fromString(row.getString("world")))
            )
        );
    }

    // Use Database builder for persistence
    public void save() {
        DataHandler.getDatabase().insert("my_objects")
            .value("uuid", uuid)
            .value("owner_uuid", owner.getUUID().toString())
            .value("x", location.getX())
            .value("y", location.getY())
            .value("z", location.getZ())
            .value("world", location.getWorld().getBukkitWorld().getUID().toString())
            .executeUpdate();
    }
}
```

---

## Quick Reference

| Task | API |
|---|---|
| Load config | `Config.load(plugin)` |
| Read config | `Config.getString("key")`, `Config.getInt("key")` |
| Load lang | `Lang.init("lang/en-us.yml")` |
| Get message | `Lang.getMessage("key")` |
| Get multi-line | `Lang.getMessageList("key")` |
| Set theme | `MessageTheme.useTheme("default")` |
| Add placeholder | `msg.copy().add("key", value)` |
| Send to player | `msg.send(player)` |
| Send action bar | `msg.sendActionbar(player)` |
| Wrap player | `SimplePlayer.of(bukkitPlayer)` |
| Wrap location | `SimpleLocation.of(bukkitLocation)` |
| Wrap world | `SimpleWorld.of(bukkitWorld)` |
| Create DB | `Database.createSqlite(file)` |
| Define table | `new TableSchema("name").addColumn(...)` |
| Query one | `db.select("t").keyEquals("k", v).one()` |
| Query list | `db.select("t").keyEquals("k", v).list()` |
| Insert | `db.insert("t").value("k", v).executeUpdate()` |
| Update | `db.update("t").value("k", v).keyEquals("k", v).executeUpdate()` |
| Delete | `db.delete("t").keyEquals("k", v).executeUpdate()` |
| Delayed task | `PlanetLib.getScheduler().runLater(task, delay, unit)` |
| Repeating task | `PlanetLib.getScheduler().runTimer(task, delay, period, unit)` |
| Log to console | `PluginLogger.info(msg)`, `PluginLogger.send(message)` |
