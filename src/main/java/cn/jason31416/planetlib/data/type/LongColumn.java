package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class LongColumn extends DataColumn {
    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.BIGINT);
            return;
        }
        if (obj instanceof Number number) {
            st.setLong(idx, number.longValue());
            return;
        }
        st.setObject(idx, obj);
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        long value = rs.getLong(key);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public String getSQLId() {
        return "BIGINT";
    }
}
