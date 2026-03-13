package cn.jason31416.planetlib.data.type;

import cn.jason31416.planetlib.util.general.Provider;
import lombok.Getter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public abstract class DataColumn {
    @Getter
    private static final Map<Class<?>, Provider<DataColumn>> types = new HashMap<>();

    static {
        registerDefaultTypes();
    }

    private boolean primaryKey;
    private boolean unique;

    public abstract void insertInStatement(PreparedStatement st, int idx, Object obj) throws SQLException;

    public abstract Object parseFromQuery(ResultSet rs, String key) throws SQLException;

    public abstract String getSQLId();

    public DataColumn setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public DataColumn setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    protected static void setNull(PreparedStatement st, int idx, int sqlType) throws SQLException {
        st.setNull(idx, sqlType);
    }

    public static void registerType(Class<?> javaType, Provider<DataColumn> provider) {
        types.put(javaType, provider);
    }

    public static DataColumn inferFromValue(Object value) {
        if (value == null) {
            return null;
        }
        Provider<DataColumn> provider = types.get(value.getClass());
        if (provider != null) {
            return provider.get();
        }
        if (value instanceof Enum<?>) {
            return new StringColumn();
        }
        return null;
    }

    private static void registerDefaultTypes() {
        registerType(Integer.class, IntegerColumn::new);
        registerType(int.class, IntegerColumn::new);
        registerType(Long.class, LongColumn::new);
        registerType(long.class, LongColumn::new);
        registerType(Double.class, DoubleColumn::new);
        registerType(double.class, DoubleColumn::new);
        registerType(Float.class, DoubleColumn::new);
        registerType(float.class, DoubleColumn::new);
        registerType(Boolean.class, BooleanColumn::new);
        registerType(boolean.class, BooleanColumn::new);
        registerType(String.class, StringColumn::new);
        registerType(byte[].class, BlobColumn::new);
    }
}
