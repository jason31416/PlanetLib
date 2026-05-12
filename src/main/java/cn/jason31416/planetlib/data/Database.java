package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.statement.DeleteStatement;
import cn.jason31416.planetlib.data.statement.InsertStatement;
import cn.jason31416.planetlib.data.statement.SelectStatement;
import cn.jason31416.planetlib.data.statement.UpdateStatement;
import cn.jason31416.planetlib.data.statement.UpsertStatement;
import cn.jason31416.planetlib.data.dialect.MySqlDialect;
import cn.jason31416.planetlib.data.dialect.SqliteDialect;
import cn.jason31416.planetlib.data.type.DataColumn;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import cn.jason31416.planetlib.data.statement.CompiledSql;
import cn.jason31416.planetlib.data.statement.SQLStatement;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class Database {
    private final SQLInstance sqlInstance;

    private final Map<String, TableSchema> tables = new ConcurrentHashMap<>();

    public Database(SQLInstance sqlInstance) {
        this.sqlInstance = sqlInstance;
    }

    public static Database createMysql(String host, int port, String database, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true");
        config.setUsername(username);
        config.setPassword(password);
        config.setPoolName("PlanetLib-MySQL");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(1);
        HikariDataSource dataSource = new HikariDataSource(config);
        return new Database(new HikariSQLInstance(dataSource, new MySqlDialect()));
    }

    public static Database createSqlite(File sqliteFile) {
        File parent = sqliteFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException("Failed to create sqlite parent directory: " + parent.getAbsolutePath());
        }
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + sqliteFile.getAbsolutePath());
        config.setPoolName("PlanetLib-SQLite");
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        HikariDataSource dataSource = new HikariDataSource(config);
        return new Database(new HikariSQLInstance(dataSource, new SqliteDialect()));
    }

    @Deprecated
    public Database(SQLInstance sqlInstance, List<Map<String, DataColumn>> structure) {
        this(sqlInstance);
        for (int i = 0; i < structure.size(); i++) {
            registerTable(new TableSchema("table_" + i).addColumns(structure.get(i)));
        }
    }

    public Database registerTable(TableSchema tableSchema) {
        tables.put(tableSchema.getTableName(), tableSchema);
        return this;
    }

    public Database registerTable(String tableName, LinkedHashMap<String, DataColumn> columns) {
        return registerTable(new TableSchema(tableName).addColumns(columns));
    }

    public Optional<TableSchema> getTable(String tableName) {
        return Optional.ofNullable(tables.get(tableName));
    }

    public Map<String, TableSchema> getTables() {
        return Map.copyOf(tables);
    }

    public void initializeSchema() {
        for (TableSchema table : tables.values()) {
            initializeTable(table);
        }
    }

    public void initializeTable(TableSchema table) {
        try (Connection conn = sqlInstance.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            if (!tableExists(metaData, table.getTableName())) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlInstance.getDialect().createTableSql(table))) {
                    stmt.execute();
                }
                return;
            }

            Set<String> existingColumns = getExistingColumns(metaData, table.getTableName());
            for (Map.Entry<String, DataColumn> entry : table.getColumns().entrySet()) {
                if (!existingColumns.contains(entry.getKey().toLowerCase(Locale.ROOT))) {
                    String sql = sqlInstance.getDialect().addColumnSql(table.getTableName(), entry.getKey(), entry.getValue());
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.execute();
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }

    public SelectStatement select(String table) {
        return new SelectStatement(sqlInstance, table);
    }

    public InsertStatement insert(String table) {
        return new InsertStatement(sqlInstance, table);
    }

    public UpdateStatement update(String table) {
        return new UpdateStatement(sqlInstance, table);
    }

    public DeleteStatement delete(String table) {
        return new DeleteStatement(sqlInstance, table);
    }

    public UpsertStatement upsert(String table) {
        UpsertStatement statement = new UpsertStatement(sqlInstance, table);
        getTable(table)
                .flatMap(TableSchema::primaryKeyColumn)
                .ifPresent(statement::primaryKey);
        return statement;
    }

    public int[] executeBatch(List<SQLStatement> statements) {
        if (statements.isEmpty()) {
            return new int[0];
        }
        try (Connection conn = sqlInstance.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int[] results = new int[statements.size()];
                for (int i = 0; i < statements.size(); i++) {
                    CompiledSql compiled = statements.get(i).compile();
                    try (PreparedStatement stmt = conn.prepareStatement(compiled.sql())) {
                        SQLInstance.bindParams(stmt, compiled.params());
                        results[i] = stmt.executeUpdate();
                    }
                }
                conn.commit();
                return results;
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Batch execution failed, transaction rolled back", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute batch", e);
        }
    }

    public CompletableFuture<int[]> executeBatchAsync(List<SQLStatement> statements) {
        return CompletableFuture.supplyAsync(() -> executeBatch(statements), sqlInstance.getAsyncExecutor());
    }

    private static boolean tableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toLowerCase(Locale.ROOT), new String[]{"TABLE"})) {
            if (rs.next()) {
                return true;
            }
        }
        try (ResultSet rs = metaData.getTables(null, null, tableName.toUpperCase(Locale.ROOT), new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private static Set<String> getExistingColumns(DatabaseMetaData metaData, String tableName) throws SQLException {
        Set<String> columns = new LinkedHashSet<>();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, null)) {
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME").toLowerCase(Locale.ROOT));
            }
        }
        return columns;
    }

    public SQLInstance getSqlInstance() {
        return sqlInstance;
    }
}
