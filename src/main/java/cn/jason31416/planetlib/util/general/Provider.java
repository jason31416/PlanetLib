package cn.jason31416.planetlib.util.general;

import org.jetbrains.annotations.Contract;

public interface Provider<T> {
    @Contract(pure = true)
    T get();
}
