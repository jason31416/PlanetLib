package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.SQLInstance;
import cn.jason31416.planetlib.util.general.Pair;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class UpdateStatement extends SQLStatement {
    private final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
    private final List<Pair<String, List<Param>>> whereConditions = new ArrayList<>();

    public UpdateStatement(SQLInstance database, String table) {
        super(database, table);
    }

    public UpdateStatement value(String column, Object value) {
        values.put(column, value);
        return this;
    }

    public UpdateStatement keyEquals(String key, Object value) {
        String condition = getDatabase().getDialect().quoteIdentifier(key) + " = ?";
        whereConditions.add(Pair.of(condition, List.of(Param.of(value))));
        return this;
    }

    public UpdateStatement where(String condition, Object... values) {
        List<Param> params = new ArrayList<>(values.length);
        for (Object value : values) {
            params.add(Param.of(value));
        }
        whereConditions.add(Pair.of(condition, params));
        return this;
    }

    @Override
    public CompiledSql compile() {
        if (values.isEmpty()) {
            throw new IllegalStateException("UpdateStatement has no values to update");
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ")
                .append(getDatabase().getDialect().quoteIdentifier(getDataTable()))
                .append(" SET ")
                .append(getDatabase().getDialect().buildSetClause(values));

        List<Param> params = new ArrayList<>();
        for (Object value : values.values()) {
            params.add(Param.of(value));
        }

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

        return new CompiledSql(sql.toString(), params);
    }
}
