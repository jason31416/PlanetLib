package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.dialect.SqliteDialect;
import cn.jason31416.planetlib.data.statement.InsertStatement;
import cn.jason31416.planetlib.data.statement.SQLStatement;
import cn.jason31416.planetlib.data.type.IntegerColumn;
import cn.jason31416.planetlib.data.type.StringColumn;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BatchExecuteTest {

    private Database database;
    private HikariDataSource dataSource;

    @BeforeEach
    void setUp() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite::memory:");
        config.setMaximumPoolSize(1);
        dataSource = new HikariDataSource(config);
        database = new Database(new HikariSQLInstance(dataSource, new SqliteDialect()));

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create test table", e);
        }
    }

    @AfterEach
    void tearDown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Test
    void executeBatch_emptyList_returnsEmptyArray() {
        int[] results = database.executeBatch(List.of());
        assertEquals(0, results.length);
    }

    @Test
    void executeBatch_multipleInserts_executesAtomically() {
        List<SQLStatement> statements = List.of(
                database.insert("users").value("id", 1).value("name", "Alice"),
                database.insert("users").value("id", 2).value("name", "Bob"),
                database.insert("users").value("id", 3).value("name", "Charlie")
        );

        int[] results = database.executeBatch(statements);

        assertEquals(3, results.length);
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);
        assertEquals(1, results[2]);

        List<String> names = queryAllNames();
        assertEquals(3, names.size());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));
        assertTrue(names.contains("Charlie"));
    }

    @Test
    void executeBatch_failureRollsBackAll() {
        List<SQLStatement> statements = List.of(
                database.insert("users").value("id", 1).value("name", "Alice"),
                database.insert("users").value("id", 1).value("name", "Duplicate"),
                database.insert("users").value("id", 3).value("name", "Charlie")
        );

        assertThrows(RuntimeException.class, () -> database.executeBatch(statements));

        List<String> names = queryAllNames();
        assertEquals(0, names.size(), "Transaction should have rolled back, no rows should exist");
    }

    @Test
    void executeBatchAsync_multipleInserts_executesAtomically() throws Exception {
        List<SQLStatement> statements = List.of(
                database.insert("users").value("id", 1).value("name", "Alice"),
                database.insert("users").value("id", 2).value("name", "Bob")
        );

        CompletableFuture<int[]> future = database.executeBatchAsync(statements);
        int[] results = future.get(5, TimeUnit.SECONDS);

        assertEquals(2, results.length);
        assertEquals(1, results[0]);
        assertEquals(1, results[1]);

        List<String> names = queryAllNames();
        assertEquals(2, names.size());
        assertTrue(names.contains("Alice"));
        assertTrue(names.contains("Bob"));
    }

    private List<String> queryAllNames() {
        List<String> names = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM users ORDER BY id")) {
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query test data", e);
        }
        return names;
    }
}
