package cn.jason31416.planetlib.util;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NbtCaseRecorder {
    private final Map<String, String> mapping = new ConcurrentHashMap<>();

    public NbtCaseRecorder() {}

    @SafeVarargs
    public final void recordCase(@NotNull Pair<String, Object>... cases) {
        for(Pair<String, Object> pair : cases)
            if(pair.value() != null)
                mapping.put(pair.key().toLowerCase(Locale.ROOT), pair.key());
    }

    public String fixCase(@NotNull String raw) {
        return mapping.getOrDefault(raw.toLowerCase(Locale.ROOT), raw);
    }
}
