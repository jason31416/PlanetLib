package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BooleanColumn extends DataColumn {
    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.BOOLEAN);
            return;
        }
        if (obj instanceof Boolean bool) {
            st.setBoolean(idx, bool);
            return;
        }
        st.setObject(idx, obj);
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        boolean value = rs.getBoolean(key);
        if (rs.wasNull()) {
            return null;
        }
        return value;
    }

    @Override
    public String getSQLId() {
        return "BOOLEAN";
    }
}
