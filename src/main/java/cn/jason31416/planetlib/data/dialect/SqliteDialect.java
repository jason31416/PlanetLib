package cn.jason31416.planetlib.data.dialect;

import cn.jason31416.planetlib.data.SqlDialect;
import cn.jason31416.planetlib.data.TableSchema;
import cn.jason31416.planetlib.data.type.DataColumn;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SqliteDialect implements SqlDialect {
    @Override
    public String id() {
        return "sqlite";
    }

    @Override
    public String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }

    @Override
    public String resolveColumnType(DataColumn column) {
        String sqlId = column.getSQLId().toUpperCase(Locale.ROOT);
        if (sqlId.contains("INT") || sqlId.contains("BOOL")) {
            return "INTEGER";
        }
        if (sqlId.contains("REAL") || sqlId.contains("DOUBLE") || sqlId.contains("FLOAT")) {
            return "REAL";
        }
        if (sqlId.contains("BLOB")) {
            return "BLOB";
        }
        return "TEXT";
    }

    @Override
    public String createTableSql(TableSchema table) {
        List<String> definitions = new ArrayList<>();
        for (Map.Entry<String, DataColumn> entry : table.getColumns().entrySet()) {
            definitions.add(createColumnDefinition(entry.getKey(), entry.getValue()));
        }
        return "CREATE TABLE IF NOT EXISTS " + quoteIdentifier(table.getTableName()) + " (" + String.join(", ", definitions) + ")";
    }

    @Override
    public String addColumnSql(String tableName, String columnName, DataColumn column) {
        return "ALTER TABLE " + quoteIdentifier(tableName) + " ADD COLUMN " + createColumnDefinition(columnName, column);
    }

    @Override
    public String upsertSql(String tableName, LinkedHashMap<String, Object> values, String primaryKeyColumn) {
        String columns = commaSeparatedIdentifiers(values.keySet());
        String placeholders = placeholders(values.size());
        List<String> updates = new ArrayList<>();
        for (String key : values.keySet()) {
            if (!key.equals(primaryKeyColumn)) {
                updates.add(quoteIdentifier(key) + "=excluded." + quoteIdentifier(key));
            }
        }
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ")
                .append(quoteIdentifier(tableName))
                .append(" (")
                .append(columns)
                .append(") VALUES (")
                .append(placeholders)
                .append(") ON CONFLICT(")
                .append(quoteIdentifier(primaryKeyColumn))
                .append(")");

        if (updates.isEmpty()) {
            sql.append(" DO NOTHING");
        } else {
            sql.append(" DO UPDATE SET ").append(String.join(", ", updates));
        }
        return sql.toString();
    }
}
