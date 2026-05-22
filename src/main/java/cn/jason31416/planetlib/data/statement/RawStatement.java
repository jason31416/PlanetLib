package cn.jason31416.planetlib.data.statement;

import cn.jason31416.planetlib.data.Param;
import cn.jason31416.planetlib.data.SQLInstance;

import java.util.List;

public class RawStatement extends SQLStatement {
    private final String sql;
    private final List<String> params;

    public RawStatement(SQLInstance database, String sql, List<String> params) {
        super(database, null);
        this.sql = sql;
        this.params = List.copyOf(params);
    }

    @Override
    public CompiledSql compile() {
        List<Param> paramList = new java.util.ArrayList<>(params.size());
        for (String value : params) {
            paramList.add(Param.of(value));
        }
        return new CompiledSql(sql, paramList);
    }
}
