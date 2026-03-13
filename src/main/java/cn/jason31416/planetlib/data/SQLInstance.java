package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.type.DataColumn;
import cn.jason31416.planetlib.util.general.ShitMountainException;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

public interface SQLInstance {
    Connection getConnection();

    SqlDialect getDialect();

    void close();

    default Executor getAsyncExecutor() {
        return ForkJoinPool.commonPool();
    }

    default int executeUpdate(String sql, List<Param> params) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error encountered when executing update '" + sql + "'", e);
        }
    }

    default boolean execute(String sql, List<Param> params) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            return stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error encountered when executing statement '" + sql + "'", e);
        }
    }

    default <T> List<T> executeQuery(String sql, List<Param> params, RowMapper<T> mapper) {
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
                return rows;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error encountered when executing query '" + sql + "'", e);
        }
    }

    default <T> Optional<T> executeQueryOne(String sql, List<Param> params, RowMapper<T> mapper) {
        List<T> rows = executeQuery(sql, params, mapper);
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(rows.getFirst());
    }

    default CompletableFuture<Integer> executeUpdateAsync(String sql, List<Param> params) {
        return CompletableFuture.supplyAsync(() -> executeUpdate(sql, params), getAsyncExecutor());
    }

    default CompletableFuture<Boolean> executeAsync(String sql, List<Param> params) {
        return CompletableFuture.supplyAsync(() -> execute(sql, params), getAsyncExecutor());
    }

    default <T> CompletableFuture<List<T>> executeQueryAsync(String sql, List<Param> params, RowMapper<T> mapper) {
        return CompletableFuture.supplyAsync(() -> executeQuery(sql, params, mapper), getAsyncExecutor());
    }

    default <T> CompletableFuture<Optional<T>> executeQueryOneAsync(String sql, List<Param> params, RowMapper<T> mapper) {
        return CompletableFuture.supplyAsync(() -> executeQueryOne(sql, params, mapper), getAsyncExecutor());
    }

    static void bindParams(PreparedStatement stmt, List<Param> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Param param = params.get(i);
            Object value = param.value();
            DataColumn type = param.column();
            if (type == null) {
                if (value == null) {
                    stmt.setNull(i + 1, Types.NULL);
                } else {
                    stmt.setObject(i + 1, value);
                }
                continue;
            }
            type.insertInStatement(stmt, i + 1, value);
        }
    }

    @Deprecated
    default ResultSet executeArgumentlessQuerySQL(String sql) {
        return executeQuerySQL(sql, List.of(), List.of());
    }

    @Deprecated
    default boolean executeArgumentlessActionSQL(String sql) {
        return executeActionSQL(sql, List.of(), List.of());
    }

    @Deprecated
    default ResultSet executeQuerySQL(String sql, List<DataColumn> types, List<Object> objects) {
        if (types.size() != objects.size()) {
            throw new ShitMountainException("Types & values count doesn't match!");
        }
        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < types.size(); i++) {
                types.get(i).insertInStatement(stmt, i + 1, objects.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                CachedRowSet cached = RowSetProvider.newFactory().createCachedRowSet();
                cached.populate(rs);
                return cached;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error encountered when executing '" + sql + "'", e);
        }
    }

    @Deprecated
    default boolean executeActionSQL(String sql, List<DataColumn> types, List<Object> objects) {
        if (types.size() != objects.size()) {
            throw new ShitMountainException("Types & values count doesn't match!");
        }
        List<Param> params = new ArrayList<>(types.size());
        for (int i = 0; i < types.size(); i++) {
            params.add(new Param(types.get(i), objects.get(i)));
        }
        return execute(sql, params);
    }
}
