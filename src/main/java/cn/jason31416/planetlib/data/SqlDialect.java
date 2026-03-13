package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.type.DataColumn;

import java.util.LinkedHashMap;
import java.util.Map;

public interface SqlDialect {
    String id();

    String quoteIdentifier(String identifier);

    String resolveColumnType(DataColumn column);

    String createTableSql(TableSchema table);

    String addColumnSql(String tableName, String columnName, DataColumn column);

    String upsertSql(String tableName, LinkedHashMap<String, Object> values, String primaryKeyColumn);

    default String createColumnDefinition(String columnName, DataColumn column) {
        StringBuilder sql = new StringBuilder();
        sql.append(quoteIdentifier(columnName)).append(' ').append(resolveColumnType(column));
        if (column.isPrimaryKey()) {
            sql.append(" PRIMARY KEY");
        }
        if (column.isUnique()) {
            sql.append(" UNIQUE");
        }
        return sql.toString();
    }

    default String commaSeparatedIdentifiers(Iterable<String> identifiers) {
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (String identifier : identifiers) {
            if (!first) {
                sql.append(", ");
            }
            first = false;
            sql.append(quoteIdentifier(identifier));
        }
        return sql.toString();
    }

    default String placeholders(int count) {
        if (count <= 0) {
            return "";
        }
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append('?');
        }
        return sql.toString();
    }

    default String buildSetClause(Map<String, Object> values) {
        StringBuilder sql = new StringBuilder();
        boolean first = true;
        for (String key : values.keySet()) {
            if (!first) {
                sql.append(", ");
            }
            first = false;
            sql.append(quoteIdentifier(key)).append(" = ?");
        }
        return sql.toString();
    }
}
