package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class DoubleColumn extends DataColumn {
    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.DOUBLE);
            return;
        }
        if (obj instanceof Number number) {
            st.setDouble(idx, number.doubleValue());
            return;
        }
        st.setObject(idx, obj);
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        double value = rs.getDouble(key);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public String getSQLId() {
        return "REAL";
    }
}
