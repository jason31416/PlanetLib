package cn.jason31416.planetlib.data.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BlobColumn extends DataColumn {
    @Override
    public void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException {
        if (obj == null) {
            setNull(st, idx, Types.BLOB);
            return;
        }
        if (obj instanceof byte[] bytes) {
            st.setBytes(idx, bytes);
            return;
        }
        st.setObject(idx, obj);
    }

    @Override
    public Object parseFromQuery(ResultSet rs, String key) throws SQLException {
        return rs.getBytes(key);
    }

    @Override
    public String getSQLId() {
        return "BLOB";
    }
}
