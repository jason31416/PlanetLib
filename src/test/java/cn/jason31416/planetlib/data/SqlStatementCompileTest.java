package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.dialect.MySqlDialect;
import cn.jason31416.planetlib.data.dialect.SqliteDialect;
import cn.jason31416.planetlib.data.statement.CompiledSql;
import cn.jason31416.planetlib.data.statement.RawStatement;
import cn.jason31416.planetlib.data.statement.SelectStatement;
import cn.jason31416.planetlib.data.statement.UpsertStatement;
import cn.jason31416.planetlib.data.type.IntegerColumn;
import cn.jason31416.planetlib.data.type.StringColumn;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlStatementCompileTest {

    @Test
    void selectStatement_shouldBuildSqlWithWhereOrderLimitOffset() {
        SQLInstance sql = new TestSQLInstance(new MySqlDialect());
        SelectStatement statement = new SelectStatement(sql, "users")
                .selectColumn("id")
                .selectColumn("name")
                .keyEquals("id", 7)
                .descending("name")
                .limit(10)
                .offset(20);

        CompiledSql compiled = statement.compile();

        assertEquals("SELECT `id`, `name` FROM `users` WHERE (`id` = ?) ORDER BY `name` DESC LIMIT ? OFFSET ?", compiled.sql());
        assertEquals(3, compiled.params().size());
        assertEquals(7, compiled.params().get(0).value());
        assertEquals(10, compiled.params().get(1).value());
        assertEquals(20, compiled.params().get(2).value());
    }

    @Test
    void upsertStatement_shouldUseMySqlDialectSyntax() {
        SQLInstance sql = new TestSQLInstance(new MySqlDialect());
        UpsertStatement statement = new UpsertStatement(sql, "users")
                .primaryKey("id")
                .value("id", 7)
                .value("name", "Alice");

        CompiledSql compiled = statement.compile();

        assertEquals(
                "INSERT INTO `users` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)",
                compiled.sql()
        );
    }

    @Test
    void upsertStatement_shouldUseSqliteDialectSyntax() {
        SQLInstance sql = new TestSQLInstance(new SqliteDialect());
        UpsertStatement statement = new UpsertStatement(sql, "users")
                .primaryKey("id")
                .value("id", 7)
                .value("name", "Alice");

        CompiledSql compiled = statement.compile();

        assertEquals(
                "INSERT INTO \"users\" (\"id\", \"name\") VALUES (?, ?) ON CONFLICT(\"id\") DO UPDATE SET \"name\"=excluded.\"name\"",
                compiled.sql()
        );
    }

    @Test
    void databaseUpsert_shouldAutoDetectPrimaryKeyFromSchema() {
        SQLInstance sql = new TestSQLInstance(new MySqlDialect());
        Database database = new Database(sql);
        database.registerTable(new TableSchema("users")
                .addColumn("id", new IntegerColumn().setPrimaryKey(true))
                .addColumn("name", new StringColumn()));

        UpsertStatement statement = database.upsert("users")
                .value("id", 1)
                .value("name", "Bob");

        assertEquals(
                "INSERT INTO `users` (`id`, `name`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `name`=VALUES(`name`)",
                statement.compile().sql()
        );
    }

    @Test
    void rawStatement_shouldKeepSqlAndBindParamsInOrder() {
        SQLInstance sql = new TestSQLInstance(new MySqlDialect());
        RawStatement statement = new RawStatement(sql,
                "SELECT * FROM users WHERE name = ? AND status = ?",
                List.of("Alice", "active"));

        CompiledSql compiled = statement.compile();

        assertEquals("SELECT * FROM users WHERE name = ? AND status = ?", compiled.sql());
        assertEquals(2, compiled.params().size());
        assertEquals("Alice", compiled.params().get(0).value());
        assertEquals("active", compiled.params().get(1).value());
    }

    private static class TestSQLInstance implements SQLInstance {
        private final SqlDialect dialect;

        private TestSQLInstance(SqlDialect dialect) {
            this.dialect = dialect;
        }

        @Override
        public Connection getConnection() {
            throw new UnsupportedOperationException("No connection required for compile tests");
        }

        @Override
        public SqlDialect getDialect() {
            return dialect;
        }

        @Override
        public void close() {
        }
    }
}
