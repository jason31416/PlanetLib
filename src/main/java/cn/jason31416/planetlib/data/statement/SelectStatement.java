package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.RowMapper;
import cn.jason31416.planetlib.data.SQLInstance;
import cn.jason31416.planetlib.data.mapper.RowMappers;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.MapTree;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SelectStatement extends SQLStatement {
    private final List<String> selectingColumns = new ArrayList<>();
    private final List<Pair<String, List<Param>>> whereConditions = new ArrayList<>();
    private final List<Pair<String, Boolean>> orderBy = new ArrayList<>();
    private Integer limit;
    private Integer offset;

    public SelectStatement(SQLInstance database, String table) {
        super(database, table);
    }

    public SelectStatement where(String condition, Object... values) {
        List<Param> params = new ArrayList<>(values.length);
        for (Object value : values) {
            params.add(Param.of(value));
        }
        whereConditions.add(Pair.of(condition, params));
        return this;
    }

    public SelectStatement keyEquals(String key, Object value) {
        String condition = getDatabase().getDialect().quoteIdentifier(key) + " = ?";
        whereConditions.add(Pair.of(condition, List.of(Param.of(value))));
        return this;
    }

    public SelectStatement limit(int limit) {
        this.limit = limit;
        return this;
    }

    public SelectStatement offset(int offset) {
        this.offset = offset;
        return this;
    }

    public SelectStatement orderBy(String column, boolean descending) {
        orderBy.add(Pair.of(column, descending));
        return this;
    }

    public SelectStatement descending(String column) {
        return orderBy(column, true);
    }

    public SelectStatement ascending(String column) {
        return orderBy(column, false);
    }

    public SelectStatement selectColumn(String key) {
        selectingColumns.add(key);
        return this;
    }

    public SelectStatement selectAll() {
        selectingColumns.clear();
        return this;
    }

    @Override
    public CompiledSql compile() {
        List<Param> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT ");
        if (selectingColumns.isEmpty()) {
            sql.append('*');
        } else {
            List<String> quotedColumns = selectingColumns.stream()
                    .map(column -> getDatabase().getDialect().quoteIdentifier(column))
                    .collect(Collectors.toList());
            sql.append(String.join(", ", quotedColumns));
        }

        sql.append(" FROM ").append(getDatabase().getDialect().quoteIdentifier(getDataTable()));

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ");
            for (int i = 0; i < whereConditions.size(); i++) {
                if (i > 0) {
                    sql.append(" AND ");
                }
                Pair<String, List<Param>> condition = whereConditions.get(i);
                sql.append('(').append(condition.first()).append(')');
                params.addAll(condition.second());
            }
        }

        if (!orderBy.isEmpty()) {
            sql.append(" ORDER BY ");
            for (int i = 0; i < orderBy.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                Pair<String, Boolean> order = orderBy.get(i);
                sql.append(getDatabase().getDialect().quoteIdentifier(order.first()));
                sql.append(order.second() ? " DESC" : " ASC");
            }
        }

        if (limit != null) {
            sql.append(" LIMIT ?");
            params.add(Param.of(limit));
        }

        if (offset != null) {
            if (limit == null) {
                sql.append(" LIMIT -1");
            }
            sql.append(" OFFSET ?");
            params.add(Param.of(offset));
        }

        return new CompiledSql(sql.toString(), params);
    }

    public List<MapTree> list() {
        return query(RowMappers.metadataMap());
    }

    public Optional<MapTree> one() {
        return queryOne(RowMappers.metadataMap());
    }

    public CompletableFuture<List<MapTree>> listAsync() {
        return queryAsync(RowMappers.metadataMap());
    }

    public CompletableFuture<Optional<MapTree>> oneAsync() {
        return queryOneAsync(RowMappers.metadataMap());
    }

    public <T> List<T> list(RowMapper<T> mapper) {
        return query(mapper);
    }

    public <T> Optional<T> one(RowMapper<T> mapper) {
        return queryOne(mapper);
    }

    public <T> CompletableFuture<List<T>> listAsync(RowMapper<T> mapper) {
        return queryAsync(mapper);
    }

    public <T> CompletableFuture<Optional<T>> oneAsync(RowMapper<T> mapper) {
        return queryOneAsync(mapper);
    }
}
