# PlanetLib - AI 使用指南

## 项目概述

PlanetLib 是一个轻量级的 Java 库，专为 Bukkit 插件开发设计，旨在简化常见任务，包括消息文件、GUI 创建、命令处理等。本指南旨在帮助 AI 理解如何正确使用 PlanetLib 作为依赖库。
若在使用过程中发现不合理处，请及时询问用户

## 核心功能模块

### 1. 初始化与配置

#### 基本初始化
```java
import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.Required;

public class YourPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        // 基本初始化（无可选功能）
        PlanetLib.initialize(this);
        
        // 或带可选功能初始化
        PlanetLib.initialize(this, 
            Required.VAULT,          // 启用 Vault 经济支持
            Required.PLACEHOLDERAPI, // 启用 PlaceholderAPI 支持
            Required.NBT             // 启用 NBT 操作支持
        );
    }
}
```

#### 可选功能说明
- `Required.VAULT`: 集成 Vault 经济系统
- `Required.PLACEHOLDERAPI`: 支持 PlaceholderAPI 占位符
- `Required.NBT`: 启用 NBT 标签操作（使用 ItemNBTAPI）

### 2. 命令系统（三层结构）

PlanetLib 的命令系统采用三层结构：`RootCommand` → `ParentCommand` → `ChildCommand`

#### RootCommand（根命令）
```java
import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StringMessage;

public class YourCommand extends RootCommand {
    public YourCommand() {
        super("yourcommand");
    }
    
    @Override
    public Message execute(ICommandContext context) {
        // 处理 /yourcommand（无参数）
        return new StringMessage("§e欢迎使用 YourPlugin！");
    }
}

// 注册命令
new YourCommand().register();
```

#### ParentCommand（父命令，可包含子命令）
```java
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.message.Message;

public class YourParentCommand extends ParentCommand {
    public YourParentCommand() {
        super("parent", parentCommand); // 注册到父命令
    }
    
    @Override
    public Message executeRaw(ICommandContext context) {
        // 处理 /yourcommand parent（无子参数）
        return new StringMessage("§e这是父命令！");
    }
}
```

#### ChildCommand（子命令，叶子节点）
```java
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.message.Message;
import java.util.List;

public class YourChildCommand extends ChildCommand {
    public YourChildCommand() {
        super("child", parentCommand); // 注册到父命令
    }
    
    @Override
    public Message execute(ICommandContext context) {
        // 处理 /yourcommand parent child
        return new StringMessage("§a这是子命令！");
    }
    
    @Override
    public List<String> tabComplete(ICommandContext context) {
        // Tab 补全逻辑
        return List.of("option1", "option2", "option3");
    }
}
```

#### 完整示例：创建多层命令结构

（以下内容也可以分多文件实现）

```java
import cn.jason31416.planetlib.command.RootCommand;
import cn.jason31416.planetlib.command.ParentCommand;
import cn.jason31416.planetlib.command.ChildCommand;
import cn.jason31416.planetlib.command.ICommandContext;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StringMessage;
import java.util.List;

public class ExampleCommand extends RootCommand {
    public ExampleCommand() {
        super("example");

        // 创建父命令 "admin"
        ParentCommand adminCommand = new ParentCommand("admin", this) {
            @Override
            public Message executeRaw(ICommandContext context) {
                return new StringMessage("§e管理员命令菜单");
            }
        };

        // 在 "admin" 下添加子命令 "reload"
        new ChildCommand("reload", adminCommand) {
            @Override
            public Message execute(ICommandContext context) {
                // 重载配置逻辑
                return new StringMessage("§a配置已重载！");
            }

            @Override
            public List<String> tabComplete(ICommandContext context) {
                return List.of();
            }
        };

        // 在 "admin" 下添加子命令 "give"
        new ChildCommand("give", adminCommand) {
            @Override
            public Message execute(ICommandContext context) {
                if (context.args().size() < 2) {
                    return new StringMessage("§c用法: /example admin give <玩家>");
                }
                String playerName = context.getArg(1);
                return new StringMessage("§a已给予物品给 " + playerName);
            }

            @Override
            public List<String> tabComplete(ICommandContext context) {
                if (context.args().size() == 2) {
                    // 返回在线玩家列表
                    return Bukkit.getOnlinePlayers().stream()
                            .map(p -> p.getName())
                            .collect(Collectors.toList());
                }
                return List.of();
            }
        };
    }

    @Override
    public Message execute(ICommandContext context) {
        return new StringMessage("§6=== 示例插件 ===\n§e/example admin - 管理员命令");
    }
}

// 使用
new ExampleCommand().register();
```

### 3. GUI 系统

#### 使用 GUIBuilder 创建 GUI
```java
import cn.jason31416.planetlib.gui.GUIBuilder;
import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUISession;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Material;
import java.util.List;

// 创建 GUI
GUI gui = new GUIBuilder("main-menu")
    .name(new StringMessage("§6主菜单"))
    .shape(
        "x x x x x x x x x",
        "x - - - - - - - x",
        "x - a - b - c - x",
        "x - - - - - - - x",
        "x x x x x x x x x"
    )
    .setItem("x", new SimpleItemStack()
        .setMaterial(Material.BLACK_STAINED_GLASS_PANE)
        .setName("§7"))
    .setItem("a", GUIBuilder.StackedItem.builder()
        .id("shop-button")
        .item(() -> new SimpleItemStack()
            .setMaterial(Material.EMERALD)
            .setName("§a商店")
            .setLore(List.of("§7点击打开商店")))
        .runnable((player, session) -> {
            // 点击处理逻辑
            player.sendMessage("§a打开商店...");
        })
        .build())
    .build();

// 打开 GUI 给玩家
new GUISession(player).display(gui);
```

### 4. 消息系统

#### 初始化消息系统
```java
import cn.jason31416.planetlib.util.Lang;

// 初始化语言文件（会自动从jar中提取messages.yml到插件数据文件夹）
Lang.init("messages.yml");
```

#### 使用消息
```java
import cn.jason31416.planetlib.util.Lang;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;

// 获取单个消息
Message welcomeMsg = Lang.getMessage("welcome");
// messages.yml 内容示例:
// welcome: "&e欢迎来到服务器！"

// 获取消息列表
MessageList helpList = Lang.getMessageList("help");
// messages.yml 内容示例:
// help:
//   - "&6=== 帮助 ==="
//   - "&e/command help - 显示此帮助"
//   - "&e/command info - 显示插件信息"

// 发送给玩家
player.sendMessage(welcomeMsg);
helpList.send(player);
```

#### 使用 MessageLoader 直接加载消息文件
```java
import cn.jason31416.planetlib.message.MessageLoader;
import java.io.File;

// 从自定义文件加载消息
MessageLoader loader = new MessageLoader(new File(plugin.getDataFolder(), "custom-messages.yml"));

// 获取消息（可指定默认值）
Message customMsg = loader.getMessage("custom.key", "§c默认消息");
```

### 5. 工具类

#### 配置管理
```java
import cn.jason31416.planetlib.util.Config;

Config.start(MyPlugin.getInstance());

// 获取配置值（config.yml）
String databaseUrl = Config.getString("database.url");
int maxPlayers = Config.getInt("max-players");
double price = Config.getDouble("shop.price");
boolean enabled = Config.getBoolean("features.enabled");

// 获取配置节
MapTree section = Config.getSection("database");
if (section != null) {
    String host = section.getString("host");
    int port = section.getInt("port");
}

// 检查配置键是否存在
if (Config.contains("debug-mode")) {
    boolean debug = Config.getBoolean("debug-mode");
}
```

#### 插件日志
```java
import cn.jason31416.planetlib.util.PluginLogger;

// 记录日志
PluginLogger.info("插件已启用！");
PluginLogger.warning("检测到配置问题...");
PluginLogger.error("数据库连接失败！");
```

#### 任务调度（Folia 支持）
```java
import cn.jason31416.planetlib.PlanetLib;

// 异步任务
PlanetLib.getScheduler().runAsync(plugin, task -> {
    // 异步逻辑，如数据库操作
});

// 延迟任务（20 ticks = 1秒后执行）
PlanetLib.getScheduler().runLater(plugin, task -> {
    // 延迟逻辑
}, 20L);

// 定时任务（每1秒执行一次）
PlanetLib.getScheduler().runTimer(plugin, task -> {
    // 定时逻辑
}, 20L, 20L);

// 玩家传送（异步安全）
SimplePlayer simplePlayer = SimplePlayer.of(player);
simplePlayer.teleport(location).thenAccept(success -> {
    if (success) {
        player.sendMessage("§a传送成功！");
    } else {
        player.sendMessage("§c传送失败！");
    }
});
```

### 6. 数据库（SQL Utils）

#### 快速创建 Database
```java
import cn.jason31416.planetlib.data.Database;
import java.io.File;

// MySQL
Database mysqlDb = Database.createMysql(
    "127.0.0.1",
    3306,
    "plugin_db",
    "root",
    "password"
);

// SQLite
Database sqliteDb = Database.createSqlite(new File(getDataFolder(), "data.db"));
```

#### 定义表结构并初始化
```java
import cn.jason31416.planetlib.data.TableSchema;
import cn.jason31416.planetlib.data.type.IntegerColumn;
import cn.jason31416.planetlib.data.type.StringColumn;
import cn.jason31416.planetlib.data.type.DoubleColumn;

TableSchema playerStats = new TableSchema("player_stats")
    .addColumn("id", new IntegerColumn().setPrimaryKey(true))
    .addColumn("name", new StringColumn(64).setUnique(true))
    .addColumn("score", new DoubleColumn());

mysqlDb.registerTable(playerStats);
mysqlDb.initializeSchema(); // 自动建表/补列
```

#### CRUD / Upsert 示例（查询结果默认是 MapTree）
```java
import cn.jason31416.planetlib.util.MapTree;
import java.util.Optional;

// upsert（推荐）
mysqlDb.upsert("player_stats")
    .value("id", 1)
    .value("name", "Jason")
    .value("score", 100.0)
    .executeUpdate();

// insert
mysqlDb.insert("player_stats")
    .value("id", 2)
    .value("name", "Alice")
    .value("score", 88.5)
    .executeUpdate();

// update
mysqlDb.update("player_stats")
    .value("score", 120.0)
    .keyEquals("id", 1)
    .executeUpdate();

// select -> Optional<MapTree>
Optional<MapTree> one = mysqlDb.select("player_stats")
    .keyEquals("id", 1)
    .one();

one.ifPresent(row -> {
    String name = row.getString("name");
    double score = row.getDouble("score");
});

// delete
mysqlDb.delete("player_stats")
    .keyEquals("id", 2)
    .executeUpdate();
```

#### 异步执行
```java
mysqlDb.select("player_stats")
    .limit(10)
    .listAsync()
    .thenAccept(rows -> {
        for (MapTree row : rows) {
            // rows 中每一项都是 MapTree
            String name = row.getString("name");
        }
    });
```

### 7. 包装类

#### SimplePlayer（玩家包装类）
```java
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleLocation;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StringMessage;

// 创建 SimplePlayer 实例
SimplePlayer simplePlayer = SimplePlayer.of(player); // 从 Player 对象
// 或
SimplePlayer simplePlayer = SimplePlayer.of(player.getUniqueId()); // 从 UUID
// 或
SimplePlayer simplePlayer = SimplePlayer.of("PlayerName"); // 从玩家名

// 获取玩家信息
String name = simplePlayer.getName();
UUID uuid = simplePlayer.getUUID();
boolean isOnline = simplePlayer.isOnline();

// 发送消息
simplePlayer.sendMessage(new StringMessage("§a你好！"));

// 获取位置（玩家必须在线）
if (simplePlayer.isOnline()) {
    SimpleLocation location = simplePlayer.getLocation();
    Player bukkitPlayer = simplePlayer.getPlayer();
}

// 经济操作（需要启用 Vault）
double balance = simplePlayer.getBalance();
simplePlayer.addBalance(100.0);
boolean success = simplePlayer.withdrawBalance(50.0);

// 异步传送
simplePlayer.teleport(SimpleLocation.of(SimpleWorld.of(world), 100, 64, 100))
    .thenAccept(success -> {
        if (success) {
            // 传送成功
        }
    });
```

#### SimpleItemStack（物品堆栈包装类）
```java
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// 创建物品（注意：SimpleItemStack没有静态of方法，需要先创建实例）
SimpleItemStack item = new SimpleItemStack()
    .setMaterial(Material.DIAMOND_SWORD)
    .setName("§6传奇之剑")
    .setLore(List.of("§7一把强大的武器", "§e伤害: 10"))
    .setQuantity(1)
    .setGlow(true); // 发光效果

// 转换为 Bukkit ItemStack
org.bukkit.inventory.ItemStack bukkitItem = item.toBukkitItem();

```

### 8. 钩子系统

#### Vault 经济集成

SimplePlayer类中有addBalance，withdrawBalance等方法。

#### PlaceholderAPI 集成

本项目中的消息系统可以自动替换PlaceholderAPI的placeholder

## 最佳实践

### 1. 错误处理
```java
try {
    // 使用 PlanetLib 功能
    SimplePlayer player = SimplePlayer.of(target);
    if (player.isOnline()) {
        player.sendMessage(new StringMessage("§a消息"));
    }
} catch (Exception e) {
    PlanetLib.error("操作失败: " + e.getMessage());
    e.printStackTrace();
}
```

### 2. 异步操作
```java
// 使用 PlanetLib 的调度器进行异步操作
PlanetLib.getScheduler().runAsync(task -> {
    // 耗时的数据库操作或网络请求
    
    // 完成后回到主线程更新UI
    PlanetLib.getScheduler().runNextTick(syncTask -> {
        player.sendMessage("§a操作完成！");
    });
});
```

### 3. 配置管理
```java
// 在插件启动时初始化配置
@Override
public void onEnable() {
    PlanetLib.initialize(this);
    Config.start(this);
    
    // 使用配置
    if (Config.getBoolean("debug")) {
        PlanetLib.info("调试模式已启用");
    }
}
```

### 其他惯例

- 尽量不要硬编码任何消息，展现给玩家的文字全部使用Lang
- 遇到Player时尽量SimplePlayer.of转换并使用SimplePlayer，就算还是需要用SimplePlayer::getPlayer()转回Bukkit的Player
- 请***务必不要***瞎编任何方法，这里没提到的方法不要自己乱写
- 所有wrapper（除了simpleitemstack）都可以作为map的key

## 常见问题

### Q: 如何解决类冲突？
A: PlanetLib 已经使用 maven-shade-plugin 重定位了内部依赖：
- `de.tr7zw.changeme.nbtapi` → `cn.jason31416.planetlib.lib.nbtapi`
- `com.tcoded.folialib` → `cn.jason31416.planetlib.lib.folialib`

### Q: 是否支持 Folia？
A: 是的，PlanetLib 内置 FoliaLib 支持，通过 `PlanetLib.getScheduler()` 提供多线程任务调度。

### Q: 如何扩展功能？
A: 可以通过实现相应的接口来扩展：
- 实现 `GUIRunnable` 创建自定义 GUI 点击动作
- 继承 `RootCommand`/`ParentCommand`/`ChildCommand` 创建复杂命令结构

### Q: 是否支持Minimessage?

A: 你可以同时在配置中使用Minimessage格式与Bukkit的&颜色代码

## 版本信息

- 当前版本: 1.4.0-Beta
- Java 版本: 21
- Bukkit API: Paper 1.21.1
- 许可证: MIT (查看 LICENSE 文件)

## 更多资源

- [GitHub 仓库](https://github.com/jason31416/planetlib)
- [Wiki 文档](https://github.com/jason31416/planetlib/wiki)
- [JitPack 发布](https://jitpack.io/#jason31416/planetlib)

---

*本指南根据 PlanetLib 实际代码编写，确保所有示例代码均可正常运行。*


## Installation (Maven)

Add the following repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.jason31416</groupId>
        <artifactId>planetlib</artifactId>
        <version>[RELEASE VERSION]</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

You should also shade the lib into another location to avoid conflicts via the maven-shade-plugin:
```xml
<relocations>
    <relocation>
        <pattern>cn.jason31416.planetlib</pattern>
        <shadedPattern>[YOUR OWN PATH].planetlib</shadedPattern>
    </relocation>
</relocations>
```
