package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class StringColumn extends DataColumn {
    private final int length;

    public StringColumn() {
        this(255);
    }

    public StringColumn(int length) {
        this.length = length;
    }

    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.VARCHAR);
            return;
        }
        st.setString(idx, String.valueOf(obj));
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        return rs.getString(key);
    }

    @Override
    public String getSQLId() {
        return "VARCHAR(" + length + ")";
    }
}
