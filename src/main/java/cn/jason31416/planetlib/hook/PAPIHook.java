package cn.jason31416.planetlib.hook;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import me.clip.placeholderapi.PlaceholderAPI;

import javax.annotation.Nullable;

public class PAPIHook {
    public static String replace(@Nullable SimplePlayer player, String text){
        return PlaceholderAPI.setPlaceholders(player==null?null:player.offlinePlayer(), text);
    }
}
