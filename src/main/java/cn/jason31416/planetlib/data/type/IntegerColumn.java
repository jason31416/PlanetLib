package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class IntegerColumn extends DataColumn {
    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.INTEGER);
            return;
        }
        if (!(obj instanceof Number number)) {
            st.setObject(idx, obj);
            return;
        }
        st.setInt(idx, number.intValue());
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        int value = rs.getInt(key);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public String getSQLId() {
        return "INT";
    }
}
