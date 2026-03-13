package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.SQLInstance;
import cn.jason31416.planetlib.util.MapTree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class InsertStatement extends SQLStatement {
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();

    public InsertStatement(SQLInstance database, String table) {
        super(database, table);
    }

    public InsertStatement value(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public InsertStatement values(LinkedHashMap<String, Object> values) {
        this.values.putAll(values);
        return this;
    }

    public InsertStatement values(MapTree values) {
        this.values.putAll(values.data);
        return this;
    }

    @Override
    public CompiledSql compile() {
        if (values.isEmpty()) {
            throw new IllegalStateException("InsertStatement has no values to insert");
        }
        String columns = getDatabase().getDialect().commaSeparatedIdentifiers(values.keySet());
        String placeholders = getDatabase().getDialect().placeholders(values.size());
        String sql = "INSERT INTO " + getDatabase().getDialect().quoteIdentifier(getDataTable()) +
                " (" + columns + ") VALUES (" + placeholders + ")";

        List<Param> params = new ArrayList<>(values.size());
        for (Object value : values.values()) {
            params.add(Param.of(value));
        }

        return new CompiledSql(sql, params);
    }
}
