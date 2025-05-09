package cn.jason31416.planetlib.data;

import java.util.UUID;

public interface IDataItem {
    UUID getUUID();
    void setUUID(UUID uuid);
    IDataItem put(String key, Object value);
    Object get(String key);
    default IDataItem set(String key, Object value){
        return put(key, value);
    }
    default String getString(String key) {
        Object value = get(key);
        if(value instanceof String) return (String) get(key);
        return null;
    }
    default Integer getInteger(String key) {
        Object value = get(key);
        if(value instanceof Integer) return (Integer) get(key);
        return null;
    }
    default Long getLong(String key) {
        Object value = get(key);
        if(value instanceof Long val) return val;
        if(value instanceof Integer val) return val.longValue();
        return null;
    }
    default Double getDouble(String key) {
        Object value = get(key);
        if(value instanceof Double) return (Double) get(key);
        return null;
    }
    default Boolean getBoolean(String key) {
        Object value = get(key);
        if(value instanceof Boolean) return (Boolean) get(key);
        return null;
    }
}
