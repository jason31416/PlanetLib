package cn.jason31416.planetlib.data.mapper;

import cn.jason31416.planetlib.data.RowMapper;
import cn.jason31416.planetlib.data.TableSchema;
import cn.jason31416.planetlib.data.type.DataColumn;
import cn.jason31416.planetlib.util.MapTree;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RowMappers {
    private RowMappers() {
    }

    public static RowMapper<MapTree> map(TableSchema schema) {
        return rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            for (Map.Entry<String, DataColumn> entry : schema.getColumns().entrySet()) {
                row.put(entry.getKey(), entry.getValue().parseFromQuery(rs, entry.getKey()));
            }
            return new MapTree(row);
        };
    }

    public static <T> RowMapper<T> singleColumn(String column, Class<T> type) {
        return rs -> type.cast(rs.getObject(column));
    }

    public static RowMapper<MapTree> metadataMap() {
        return rs -> {
            Map<String, Object> row = new LinkedHashMap<>();
            var meta = rs.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            return new MapTree(row);
        };
    }
}
