package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.RowMapper;
import cn.jason31416.planetlib.data.SQLInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public abstract class SQLStatement {
    private final SQLInstance database;
    private final String dataTable;

    protected SQLStatement(SQLInstance database, String dataTable) {
        this.database = database;
        this.dataTable = dataTable;
    }

    public abstract CompiledSql compile();

    public PreparedStatement prepareStatement(Connection connection) throws SQLException {
        CompiledSql compiled = compile();
        PreparedStatement stmt = connection.prepareStatement(compiled.sql());
        SQLInstance.bindParams(stmt, compiled.params());
        return stmt;
    }

    public int executeUpdate() {
        CompiledSql compiled = compile();
        return database.executeUpdate(compiled.sql(), compiled.params());
    }

    public boolean execute() {
        CompiledSql compiled = compile();
        return database.execute(compiled.sql(), compiled.params());
    }

    public <T> List<T> query(RowMapper<T> mapper) {
        CompiledSql compiled = compile();
        return database.executeQuery(compiled.sql(), compiled.params(), mapper);
    }

    public <T> Optional<T> queryOne(RowMapper<T> mapper) {
        CompiledSql compiled = compile();
        return database.executeQueryOne(compiled.sql(), compiled.params(), mapper);
    }

    public CompletableFuture<Integer> executeUpdateAsync() {
        CompiledSql compiled = compile();
        return database.executeUpdateAsync(compiled.sql(), compiled.params());
    }

    public CompletableFuture<Boolean> executeAsync() {
        CompiledSql compiled = compile();
        return database.executeAsync(compiled.sql(), compiled.params());
    }

    public <T> CompletableFuture<List<T>> queryAsync(RowMapper<T> mapper) {
        CompiledSql compiled = compile();
        return database.executeQueryAsync(compiled.sql(), compiled.params(), mapper);
    }

    public <T> CompletableFuture<Optional<T>> queryOneAsync(RowMapper<T> mapper) {
        CompiledSql compiled = compile();
        return database.executeQueryOneAsync(compiled.sql(), compiled.params(), mapper);
    }

    protected List<Param> inferredParams(Iterable<Object> values) {
        return toParams(values);
    }

    protected static List<Param> toParams(Iterable<Object> values) {
        java.util.ArrayList<Param> params = new java.util.ArrayList<>();
        for (Object value : values) {
            params.add(Param.of(value));
        }
        return params;
    }

    public SQLInstance getDatabase() {
        return database;
    }

    public String getDataTable() {
        return dataTable;
    }
}
