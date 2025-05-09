package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nullable;

public interface PlaceholderHandler {
    String replacePlaceholders(String message, @Nullable SimplePlayer player);
}
