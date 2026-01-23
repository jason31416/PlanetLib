package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.util.general.Pair;

import java.util.List;

public record DataTable(String tableName, String primaryKey, DataStorage storage, List<Pair<String, Class<?>>> columns) {
}