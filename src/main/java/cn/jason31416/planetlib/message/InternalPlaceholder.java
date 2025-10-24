package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.PAPIHook;
import cn.jason31416.planetlib.wrapper.SimplePlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InternalPlaceholder {
    public static List<PlaceholderHandler> placeholderHandlers = new ArrayList<>();
    public static void registerPlaceholderHandler(PlaceholderHandler handler){
        placeholderHandlers.add(handler);
    }
    public static String replacePlaceholders(String message, @Nullable SimplePlayer player){
        for(PlaceholderHandler handler : placeholderHandlers){
            message = handler.replacePlaceholders(message, player);
        }
        return PlanetLib.isPAPIEnabled?PAPIHook.replace(player, message):message;
    }
}
