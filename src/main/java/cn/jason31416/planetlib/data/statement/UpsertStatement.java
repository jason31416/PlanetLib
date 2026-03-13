package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.SQLInstance;
import cn.jason31416.planetlib.util.MapTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class UpsertStatement extends SQLStatement {
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    private String primaryKey;

    public UpsertStatement(SQLInstance database, String table) {
        super(database, table);
    }

    public UpsertStatement value(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public UpsertStatement values(LinkedHashMap<String, Object> values) {
        this.values.putAll(values);
        return this;
    }

    public UpsertStatement values(MapTree values) {
        this.values.putAll(values.data);
        return this;
    }

    public UpsertStatement primaryKey(String primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    @Override
    public CompiledSql compile() {
        if (values.isEmpty()) {
            throw new IllegalStateException("UpsertStatement has no values");
        }
        if (primaryKey == null || primaryKey.isBlank()) {
            throw new IllegalStateException("UpsertStatement requires primaryKey(...) before compile");
        }

        String sql = getDatabase().getDialect().upsertSql(getDataTable(), values, primaryKey);
        List<Param> params = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            params.add(Param.of(value));
        }
        return new CompiledSql(sql, params);
    }
}
