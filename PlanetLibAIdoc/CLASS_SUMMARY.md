# Command 包

## ChildCommand
- 实现的接口: ICommand
- 成员变量: 无
- 方法头:
  - ChildCommand(String name, IParentCommand parent)
  - ChildCommand(List<String> names, IParentCommand parent)
  - abstract Message execute(ICommandContext context)
  - abstract List<String> tabComplete(ICommandContext context)

## CommandContext
- 实现的接口: ICommandContext
- 成员变量:
  - final List<String> args
  - final SimpleSender sender
  - final SimplePlayer player
  - String commandName
- 方法头:
  - CommandContext(List<String> args, SimpleSender sender, SimplePlayer player, String commandName)
  - ICommandContext getSubContext()
  - String getArg(int index)
  - String getUsage(ParameterType... parameterTypes)
  - SimplePlayer player()
  - SimpleSender sender()
  - List<String> args()

## PaperRootCommandHandler
- 成员变量: 无
- 方法头:
  - static void register(RootCommand rootCommand)
  - void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args)
  - @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args)

## ParameterType
- 成员变量: 无
- 方法头: 无

## ParentCommand
- 实现的接口: ICommand, IParentCommand
- 成员变量: 无
- 方法头:
  - ParentCommand(String name, IParentCommand parent)
  - ParentCommand(List<String> names, IParentCommand parent)
  - Message execute(ICommandContext context)
  - List<String> tabComplete(ICommandContext context)
  - void registerSubCommand(String name, ICommand command)
  - abstract Message executeRaw(ICommandContext context)

## RootCommand
- 实现的接口: ICommand, IParentCommand
- 成员变量:
  - String name
- 方法头:
  - RootCommand(String name)
  - void register()
  - void paperRegister()
  - void bukkitRegister()
  - boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] strings)
  - List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String[] strings)
  - void registerSubCommand(String name, ICommand command)
  - List<String> tabComplete(ICommandContext context)
  - static Builder builder(String name)
  - @Nullable Message execute(ICommandContext context)

# Data 包

## Database
- 成员变量:
  - SQLInstance sqlInstance
  - Map<String, TableSchema> tables
- 方法头:
  - Database(SQLInstance sqlInstance)
  - static Database createMysql(String host, int port, String database, String username, String password)
  - static Database createSqlite(File sqliteFile)
  - @Deprecated Database(SQLInstance sqlInstance, List<Map<String, DataColumn>> structure)
  - Database registerTable(TableSchema tableSchema)
  - Database registerTable(String tableName, LinkedHashMap<String, DataColumn> columns)
  - Optional<TableSchema> getTable(String tableName)
  - Map<String, TableSchema> getTables()
  - void initializeSchema()
  - void initializeTable(TableSchema table)
  - SelectStatement select(String table)
  - InsertStatement insert(String table)
  - UpdateStatement update(String table)
  - DeleteStatement delete(String table)
  - UpsertStatement upsert(String table)
  - SQLInstance getSqlInstance()

## SQLInstance
- 方法头:
  - Connection getConnection()
  - SqlDialect getDialect()
  - void close()
  - default Executor getAsyncExecutor()
  - default int executeUpdate(String sql, List<Param> params)
  - default boolean execute(String sql, List<Param> params)
  - default <T> List<T> executeQuery(String sql, List<Param> params, RowMapper<T> mapper)
  - default <T> Optional<T> executeQueryOne(String sql, List<Param> params, RowMapper<T> mapper)
  - default CompletableFuture<Integer> executeUpdateAsync(String sql, List<Param> params)
  - default CompletableFuture<Boolean> executeAsync(String sql, List<Param> params)
  - default <T> CompletableFuture<List<T>> executeQueryAsync(String sql, List<Param> params, RowMapper<T> mapper)
  - default <T> CompletableFuture<Optional<T>> executeQueryOneAsync(String sql, List<Param> params, RowMapper<T> mapper)

## HikariSQLInstance
- 实现的接口: SQLInstance
- 方法头:
  - HikariSQLInstance(HikariDataSource dataSource, SqlDialect dialect)
  - HikariSQLInstance(HikariDataSource dataSource, SqlDialect dialect, Executor asyncExecutor)
  - Connection getConnection()
  - void close()
  - Executor getAsyncExecutor()
  - SqlDialect getDialect()
  - HikariDataSource getDataSource()

## TableSchema
- 成员变量:
  - String tableName
  - LinkedHashMap<String, DataColumn> columns
- 方法头:
  - TableSchema(String tableName)
  - TableSchema addColumn(String name, DataColumn column)
  - TableSchema addColumns(Map<String, DataColumn> columnMap)
  - Optional<String> primaryKeyColumn()
  - String getTableName()
  - LinkedHashMap<String, DataColumn> getColumns()

## Param
- 成员变量:
  - DataColumn column
  - Object value
- 方法头:
  - static Param of(Object value)
  - static Param of(DataColumn column, Object value)

## RowMapper
- 方法头:
  - T map(ResultSet rs) throws SQLException

## SelectStatement / InsertStatement / UpdateStatement / DeleteStatement / UpsertStatement
- 继承的类: SQLStatement
- 常用方法头:
  - `SelectStatement`: `where(...)`, `keyEquals(...)`, `limit(...)`, `offset(...)`, `orderBy(...)`, `descending(...)`, `ascending(...)`, `selectColumn(...)`, `selectAll()`, `List<MapTree> list()`, `Optional<MapTree> one()`, 异步 `listAsync()/oneAsync()`
  - `InsertStatement`: `value(...)`, `values(LinkedHashMap<String, Object>)`, `values(MapTree)`, `compile()`
  - `UpdateStatement`: `value(...)`, `where(...)`, `keyEquals(...)`, `compile()`
  - `DeleteStatement`: `where(...)`, `keyEquals(...)`, `compile()`
  - `UpsertStatement`: `primaryKey(...)`, `value(...)`, `values(LinkedHashMap<String, Object>)`, `values(MapTree)`, `compile()`

## DataColumn（及常用实现）
- 抽象方法:
  - `insertInStatement(PreparedStatement st, int idx, Object obj)`
  - `parseFromQuery(ResultSet rs, String key)`
  - `getSQLId()`
- 标志位:
  - `setPrimaryKey(boolean)` / `isPrimaryKey()`
  - `setUnique(boolean)` / `isUnique()`
- 常用实现:
  - `IntegerColumn`
  - `StringColumn`
  - `LongColumn`
  - `DoubleColumn`
  - `BooleanColumn`
  - `BlobColumn`

# Message 包

## StringMessage
- 实现的接口: Message
- 成员变量:
  - static MiniMessage miniMessage
  - String content
- 方法头:
  - StringMessage(String content)
  - StringMessage add(String placeholder, Object value)
  - String toString()
  - String toFormatted()
  - Message copy()
  - Component toComponent()
  - void send(CommandSender player)
  - void sendActionbar(Player player)
  - boolean equals(Object obj)

## MessageLoader
- 成员变量:
  - ConfigurationSection messageConfig
- 方法头:
  - MessageLoader(ConfigurationSection messageConfig)
  - MessageLoader(File filePath)
  - StringMessage getMessage(String key, String defaultMessage)
  - String getRawMessage(String key, String defaultMessage)
  - MessageList getList(String key, List<String> defaultList)

## InternalPlaceholder
- 成员变量: 无
- 方法头:
  - static void registerPlaceholderHandler(PlaceholderHandler handler)
  - static String replacePlaceholders(String message, @Nullable SimplePlayer player)

## MessageList
- 实现的接口: Message
- 成员变量:
  - final List<String> content
- 方法头:
  - MessageList(List<String> content)
  - MessageList add(String placeholder, Object value)
  - MessageList copy()
  - void send(CommandSender sender)
  - void sendActionbar(Player sender)
  - Component toComponent()
  - void add(String message)
  - void remove(int index)
  - List<String> asList()

# Util 包

## Util
- 成员变量: 无
- 方法头:
  - static void saveFolder(String name)
  - static void savePluginResource(@NotNull String resourcePath)
  - static <T> @Nullable T getFirstNonnullOne(T... objs)
  - static @Nullable Integer tryParseInt(@NotNull String st)

## PluginLogger
- 成员变量: 无
- 方法头:
  - static void info(String message)
  - static void warning(String message)
  - static void error(String message)
  - static void send(Message message)

## Lang
- 成员变量:
  - static MessageLoader messageLoader
- 方法头:
  - static void init(String fileName)
  - static Message getMessage(String key)
  - static MessageList getMessageList(String key)

## MapTree
- 实现的接口: Serializable
- 成员变量:
  - Map<String, Object> data
- 方法头:
  - MapTree(Map<String, Object> data)
  - MapTree()
  - MapTree put(String key, Object val)
  - Object get(String key)
  - MapTree getSection(String key)
  - boolean getBoolean(String key, boolean defaultValue)
  - boolean getBoolean(String key)
  - boolean contains(String key)
  - int getInt(String key, int defaultValue)
  - int getInt(String key)
  - double getDouble(String key, double defaultValue)
  - double getDouble(String key)
  - String getString(String key, String defaultValue)
  - String getString(String key)
  - List<String> getStringList(String key)
  - Set<String> getKeys()
  - String toJson()
  - String toYaml()
  - static MapTree fromJson(String json)
  - static MapTree fromYaml(String yaml)

## Config
- 成员变量:
  - static MapTree configTree
- 方法头:
  - static void start(JavaPlugin plugin)
  - static Object getItem(String key)
  - static MapTree getSection(String key)
  - static int getInt(String key)
  - static double getDouble(String key)
  - static String getString(String key)
  - static boolean getBoolean(String key)
  - static boolean contains(String key)

## TimedHashMap
- 成员变量:
  - final WrappedTask cleanTask
  - final long defaultCleanMillis
- 方法头:
  - TimedHashMap(long cleanMillis)
  - Map<K, V> snapshot()
  - int size()
  - boolean isEmpty()
  - boolean containsKey(Object key)
  - boolean containsValue(Object value)
  - V get(Object key)
  - V put(K key, V value)
  - V put(K key, V value, long cleanMillis)
  - V remove(Object key)
  - void putAll(@NonNull Map<? extends K, ? extends V> m)
  - void clear()
  - @NonNull Set<K> keySet()
  - @NonNull Collection<V> values()
  - @NonNull Set<Entry<K, V>> entrySet()
  - void close()

## Pair (util/general)
- 成员变量:
  - final T first
  - final U second
- 方法头:
  - Pair(T first, U second)
  - T first()
  - U second()
  - boolean equals(Object obj)
  - int hashCode()
  - String toString()

## Provider (util/general)
- 成员变量: 无
- 方法头:
  - T get()

## ShitMountainException
- 继承的类: RuntimeException
- 成员变量: 无
- 方法头:
  - ShitMountainException(String message, Exception e)
  - ShitMountainException(String message)

# Wrapper 包

## SimplePlayer
- 实现的接口: ConfigurationSerializable, Serializable
- 成员变量:
  - transient OfflinePlayer offlinePlayer
- 方法头:
  - SimplePlayer(OfflinePlayer offlinePlayer)
  - String getName()
  - UUID getUUID()
  - boolean isOnline()
  - SimpleLocation getLocation()
  - Player getPlayer()
  - void sendMessage(Message message)
  - void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut)
  - double getBalance()
  - void addBalance(double amount)
  - boolean withdrawBalance(double amount)
  - boolean equals(Object obj)
  - CompletableFuture<Boolean> teleport(SimpleLocation location)
  - int hashCode()
  - static SimplePlayer of(OfflinePlayer offlinePlayer)
  - static SimplePlayer of(Player player)
  - static SimplePlayer of(CommandSender player)
  - static SimplePlayer of(UUID uuid)
  - static SimplePlayer of(String name)
  - Map<String, Object> serialize()
  - static SimplePlayer deserialize(@NotNull Map<String, Object> map)
  - OfflinePlayer offlinePlayer()
  - String toString()
  - void writeObject(ObjectOutputStream out)
  - void readObject(ObjectInputStream in)

## SimpleItemStack
- 成员变量:
  - Message name
  - int quantity
  - boolean glow
  - Map<Enchantment, Integer> enchantments
  - int customModelData
  - Material material
  - MessageList lore
  - String skullId
  - ItemStack stack
- 方法头:
  - SimpleItemStack setMaterial(Material material)
  - SimpleItemStack setItemStack(ItemStack stack)
  - void setAsVanillaItemStack(ItemStack stack)
  - SimpleItemStack setName(String name)
  - SimpleItemStack setName(Message name)
  - SimpleItemStack setQuantity(int quantity)
  - SimpleItemStack setLore(List<String> lore)
  - SimpleItemStack setLore(MessageList lore)
  - SimpleItemStack setGlow(boolean glow)
  - SimpleItemStack setCustomModelData(int data)
  - SimpleItemStack setSkullID(String skullID)
  - SimpleItemStack placeholder(String placeholder, String value)
  - SimpleItemStack papi(SimplePlayer player)
  - ItemStack toBukkitItem()
  - SimpleItemStack copy()

## SimpleChunkLocation
- 实现的接口: ConfigurationSerializable, Serializable
- 成员变量:
  - final int x
  - final int z
  - final SimpleWorld world
- 方法头:
  - SimpleChunkLocation(int x, int z, SimpleWorld world)
  - World getBukkitWorld()
  - Chunk getChunk()
  - SimpleChunkLocation getRelative(int dx, int dz)
  - SimpleChunkLocation getRelative(Direction dir)
  - boolean equals(Object obj)
  - String toString()
  - static SimpleChunkLocation of(Chunk chunk)
  - double distance(SimpleChunkLocation other)
  - Collection<SimpleChunkLocation> getAdjacentChunks()
  - Collection<SimpleChunkLocation> getDiagAdjacentChunks()
  - Collection<SimpleChunkLocation> getEightAdjacentChunks()
  - static SimpleChunkLocation of(int x, int z, SimpleWorld world)
  - static SimpleChunkLocation of(int x, int z)
  - Map<String, Object> serialize()
  - static SimpleChunkLocation deserialize(Map<String, Object> map)
  - int x()
  - int z()
  - SimpleWorld world()
  - int hashCode()

## SimpleSender
- 成员变量:
  - final CommandSender sender
- 方法头:
  - SimpleSender(CommandSender sender)
  - static SimpleSender of(@NotNull CommandSender sender)
  - boolean isPlayer()
  - SimplePlayer toPlayer()
  - void sendMessage(Message message)
  - CommandSender sender()
  - boolean equals(Object obj)
  - int hashCode()
  - String toString()

## SimpleWorld
- 实现的接口: Serializable
- 成员变量:
  - transient World world
- 方法头:
  - SimpleWorld(World world)
  - World getBukkitWorld()
  - String getName()
  - static SimpleWorld of(World world)
  - static SimpleWorld of(UUID worldUUID)
  - static SimpleWorld of(String worldName)
  - static SimpleWorld defaultWorld()
  - boolean equals(Object obj)
  - int hashCode()
  - String toString()
  - void writeObject(ObjectOutputStream out)
  - void readObject(ObjectInputStream in)

## SimpleLocation
- 实现的接口: ConfigurationSerializable, Serializable
- 成员变量:
  - final double x
  - final double y
  - final double z
  - final SimpleWorld world
- 方法头:
  - SimpleLocation(double x, double y, double z, SimpleWorld world)
  - SimpleLocation getRelative(double dx, double dy, double dz)
  - SimpleLocation getBlockLocation()
  - Block getBlock()
  - Location getBukkitLocation()
  - SimpleChunkLocation getChunkLocation()
  - static SimpleLocation of(Location location)
  - static SimpleLocation of(Block block)
  - static SimpleLocation of(double x, double y, double z, SimpleWorld world)
  - CompletableFuture<Block> getLoadedBlock()
  - Material getBlockMaterial()
  - void setBlockMaterial(Material material)
  - boolean equals(Object obj)
  - int hashCode()
  - Map<String, Object> serialize()
  - static SimpleLocation deserialize(Map<String, Object> map)
  - double x()
  - double y()
  - double z()
  - SimpleWorld world()
  - String toString()
