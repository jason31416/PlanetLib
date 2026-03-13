package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;

import java.util.List;

public record CompiledSql(String sql, List<Param> params) {
}
