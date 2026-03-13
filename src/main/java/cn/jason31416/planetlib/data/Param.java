package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.data.type.DataColumn;

public record Param(DataColumn column, Object value) {
    public static Param of(Object value) {
        return new Param(DataColumn.inferFromValue(value), value);
    }

    public static Param of(DataColumn column, Object value) {
        return new Param(column, value);
    }
}
