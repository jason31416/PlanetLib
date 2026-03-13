package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.type.DataColumn;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class TableSchema {
    private final String tableName;
    private final LinkedHashMap<String, DataColumn> columns = new LinkedHashMap<>();

    public TableSchema(String tableName) {
        this.tableName = tableName;
    }

    public TableSchema addColumn(String name, DataColumn column) {
        columns.put(name, column);
        return this;
    }

    public TableSchema addColumns(Map<String, DataColumn> columnMap) {
        columns.putAll(columnMap);
        return this;
    }

    public Optional<String> primaryKeyColumn() {
        return columns.entrySet().stream()
                .filter(entry -> entry.getValue().isPrimaryKey())
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public String getTableName() {
        return tableName;
    }

    public LinkedHashMap<String, DataColumn> getColumns() {
        return columns;
    }
}
