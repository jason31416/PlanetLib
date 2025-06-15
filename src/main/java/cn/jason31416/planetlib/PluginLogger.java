package cn.jason31416.planetlib;

import cn.jason31416.planetlib.message.Message;
import org.bukkit.Bukkit;

public class PluginLogger {
    public static void info(String message) {
        PlanetLib.instance.getLogger().info(message);
    }
    public static void warning(String message) {
        PlanetLib.instance.getLogger().warning(message);
    }
    public static void error(String message) {
        PlanetLib.instance.getLogger().severe(message);
    }
    public static void send(Message message){
        message.send(Bukkit.getConsoleSender());
    }
}
