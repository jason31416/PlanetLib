package cn.jason31416.planetlib.util;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class Config {
    public static FileConfiguration config;
    public static void start(JavaPlugin plugin) {
        config = plugin.getConfig();
    }
    public static FileConfiguration getFile(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }
    public static FileConfiguration getConfig() {
        return config;
    }

    public static Object get(String path) {
        return config.get(path);
    }
    public static Object get(String path, Object def){
        return config.get(path, def);
    }
    public static String getString(String path){
        return config.getString(path);
    }
    public static String getString(String path, String def){
        return config.getString(path, def);
    }
    public static int getInt(String path){
        return config.getInt(path);
    }
    public static int getInt(String path, int def){
        return config.getInt(path, def);
    }
    public static double getDouble(String path){
        return config.getDouble(path);
    }
    public static double getDouble(String path, double def){
        return config.getDouble(path, def);
    }
    public static boolean getBoolean(String path){
        return config.getBoolean(path);
    }
    public static boolean getBoolean(String path, boolean def){
        return config.getBoolean(path, def);
    }

    public static boolean contains(String path){
        return config.contains(path);
    }
    public static Set<String> getKeys(@Nullable String path){
        if(path == null||path.isEmpty()) {
            return config.getKeys(false);
        }
        ConfigurationSection confSec = config.getConfigurationSection(path);
        if(confSec == null) {
            return new HashSet<>();
        }
        return confSec.getKeys(false);
    }
}
