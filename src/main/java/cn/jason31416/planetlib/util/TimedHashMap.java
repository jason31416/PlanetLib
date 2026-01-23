package cn.jason31416.planetlib.util;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.util.general.Pair;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimedHashMap<K, V> implements Map<K,V> {
    @Getter
    private final ConcurrentHashMap<K, Pair<Long, V>> map=new ConcurrentHashMap<>();
    private final WrappedTask cleanTask;
    private final long defaultCleanMillis;
    public TimedHashMap(long cleanMillis) {
        this.defaultCleanMillis = cleanMillis;
        cleanTask = PlanetLib.getScheduler().runTimerAsync(()->{
            for(var item: new HashSet<>(map.entrySet())) {
                if(item.getValue().first() < System.currentTimeMillis()) {
                    map.remove(item.getKey());
                }
            }
        }, cleanMillis, cleanMillis, TimeUnit.MILLISECONDS);
    }

    public Map<K, V> snapshot() {
        Map<K,V> snapshot=new HashMap<>();
        for(var item: new HashSet<>(map.entrySet())) {
            snapshot.put(item.getKey(), item.getValue().second());
        }
        return snapshot;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override @Deprecated
    public boolean containsValue(Object value) {
        return snapshot().containsValue(value);
    }

    @Override
    public V get(Object key) {
        return map.get(key).second();
    }

    public V put(K key, V value) {
        map.put(key, new Pair<>(System.currentTimeMillis()+defaultCleanMillis, value));
        return value;
    }

    public V put(K key, V value, long cleanMillis) {
        map.put(key, new Pair<>(System.currentTimeMillis()+cleanMillis, value));
        return value;
    }

    @Override
    public V remove(Object key) {
        return map.remove(key).second();
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        for(var entry: m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public @NonNull Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public @NonNull Collection<V> values() {
        return map.values().stream().map(Pair::second).toList();
    }

    @Override @Deprecated
    public @NonNull Set<Entry<K, V>> entrySet() {
        return snapshot().entrySet();
    }

    public void close(){
        cleanTask.cancel();
    }
}
