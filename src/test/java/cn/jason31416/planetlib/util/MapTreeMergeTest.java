package cn.jason31416.planetlib.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MapTreeMergeTest {

    @Test
    void merge_shouldRecursivelyMergeNestedMap_andKeepLeftOnConflict() {
        MapTree left = new MapTree(new ConcurrentHashMap<>(Map.of(
                "a", new ConcurrentHashMap<>(Map.of("b", 1)),
                "c", 2
        )));
        MapTree right = new MapTree(new ConcurrentHashMap<>(Map.of(
                "a", new ConcurrentHashMap<>(Map.of("c", 2)),
                "c", 3
        )));

        left.merge(right);

        Map<String, Object> mergedA = (Map<String, Object>) left.get("a");
        assertEquals(1, mergedA.get("b"));
        assertEquals(2, mergedA.get("c"));
        assertEquals(2, left.get("c"));
    }
}
