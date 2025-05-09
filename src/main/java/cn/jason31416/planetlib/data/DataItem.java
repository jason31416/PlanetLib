package cn.jason31416.planetlib.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataItem implements IDataItem {
    private UUID uuid;
    public HashMap<String, Object> data=new HashMap<>();
    public DataItem(UUID uuid) {
        this.uuid = uuid;
    }
    public DataItem() {
        this.uuid = UUID.randomUUID();
    }
    public void setUUID(UUID uuid) {
        this.uuid = uuid;
    }
    public DataItem putAll(Map<String, Object> map) {
        data.putAll(map);
        return this;
    }
    public DataItem put(String key, Object value) {
        data.put(key, value);
        return this;
    }
    public Object get(String key) {
        return data.get(key);
    }
    public UUID getUUID() {
        return uuid;
    }
}
