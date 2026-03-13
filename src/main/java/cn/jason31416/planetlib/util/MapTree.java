package cn.jason31416.planetlib.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.yaml.snakeyaml.Yaml;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unchecked")
public class MapTree implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    public Map<String, Object> data;
    public MapTree(Map<String, Object> data){
        this.data = data;
    }
    public MapTree(){
        this.data = new ConcurrentHashMap<>();
    }

    public MapTree merge(MapTree other){
        // Recursively merge this maptree with the other. If any overlapping key exists, this tree should be prioritized
        if (other == null || other.data == null) {
            return this;
        }
        mergeMaps(this.data, other.data);
        return this;
    }

    private static void mergeMaps(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object sourceValue = entry.getValue();

            if (!target.containsKey(key) || target.get(key) == null) {
                target.put(key, deepCopyValue(sourceValue));
                continue;
            }

            Object targetValue = target.get(key);
            Map<String, Object> targetMap = asMap(targetValue);
            Map<String, Object> sourceMap = asMap(sourceValue);
            if (targetMap != null && sourceMap != null) {
                mergeMaps(targetMap, sourceMap);
            }
        }
    }

    private static Map<String, Object> asMap(Object value) {
        if (value instanceof MapTree mapTree) {
            return mapTree.data;
        }
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    public static Object deepCopyValue(Object value) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> copied = new ConcurrentHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                copied.put(String.valueOf(entry.getKey()), deepCopyValue(entry.getValue()));
            }
            return copied;
        }

        if (value instanceof List<?> listValue) {
            List<Object> copied = new ArrayList<>(listValue.size());
            for (Object item : listValue) {
                copied.add(deepCopyValue(item));
            }
            return copied;
        }

        return value;
    }

    public MapTree put(String key, Object val){
        try {
            if (key.contains(".")) {
                String[] keys = key.split("\\.");
                if(!data.containsKey(keys[0])) data.put(keys[0], new ConcurrentHashMap<>());
                Map<String, Object> value = (Map<String, Object>) data.get(keys[0]);
                for (int i = 1; i < keys.length-1; i++) {
                    if(!data.containsKey(keys[0])) value.put(keys[0], new ConcurrentHashMap<>());
                    value = (Map<String, Object>) value.get(keys[0]);
                }
                value.put(keys[keys.length-1], val);
            }else{
                data.put(key, val);
            }
            return this;
        } catch (Exception e) {
            e.printStackTrace();
            return this;
        }
    }

    public Object get(String key) {
        try {
            if (key.contains(".")) {
                String[] keys = key.split("\\.");
                Object value = data.get(keys[0]);
                for (int i = 1; i < keys.length; i++) {
                    value = ((Map<String, Object>) value).get(keys[i]);
                }
                return value;
            } else {
                return data.get(key);
            }
        } catch (Exception e) {
            return null;
        }
    }

    public MapTree getSection(String key){
        Object value = get(key);
        if(value instanceof Map){
            return new MapTree((Map<String, Object>) value);
        }
        return new MapTree(new ConcurrentHashMap<>());
    }

    public boolean getBoolean(String key, boolean defaultValue){
        Object value = get(key);
        if(value instanceof Boolean){
            return (Boolean) value;
        }
        return defaultValue;
    }
    public boolean getBoolean(String key){
        return getBoolean(key, false);
    }
    public boolean contains(String key){
        return get(key)!= null;
    }
    public int getInt(String key, int defaultValue){
        Object value = get(key);
        if(value instanceof Integer){
            return (Integer) value;
        }else if(value instanceof Double) {
            return ((Double) value).intValue();
        }else if(value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    public int getInt(String key){
        return getInt(key, 0);
    }
    public double getDouble(String key, double defaultValue){
        Object value = get(key);
        if(value instanceof Double){
            return (Double) value;
        }else if(value instanceof Integer) {
            return ((Integer) value).doubleValue();
        }else if(value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    public double getDouble(String key){
        return getDouble(key, 0.0);
    }
    public String getString(String key, String defaultValue){
        Object value = get(key);
        if(value instanceof String){
            return (String) value;
        }
        return defaultValue;
    }
    public String getString(String key){
        return getString(key, "");
    }
    public List<String> getStringList(String key){
        Object value = get(key);
        if(value instanceof List){
            return (List<String>) value;
        }
        return new ArrayList<>();
    }
    public boolean isList(String key){
        return contains(key) && get(key) instanceof List;
    }
    public Set<String> getKeys(){
        return data.keySet();
    }

    public String toJson(){
        return new Gson().toJson(data);
    }
    public String toYaml(){
        return new Yaml().dump(data);
    }
    public static MapTree fromJson(String json){
        return new MapTree(new Gson().fromJson(json, new TypeToken<Map<String, Object>>(){}.getType()));
    }
    public static MapTree fromYaml(String yaml){
        return new MapTree(new Yaml().load(yaml));
    }
}
