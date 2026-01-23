package cn.jason31416.planetlib.util;

import cn.jason31416.planetlib.PlanetLib;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Objects;

@SuppressWarnings("unused")
public class Config {
    @Getter
    private static MapTree configTree;

    public static void start(JavaPlugin plugin) {
        Util.savePluginResource("config.yml");
        File config = new File(PlanetLib.getInstance().getDataFolder(), "config.yml");

        try(InputStream inputStream = new FileInputStream(config)) {
            configTree = new MapTree(new Yaml().load(inputStream));
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static Object getItem(String key) {
        return configTree.get(key);
    }

    public static MapTree getSection(String key){
        return configTree.getSection(key);
    }
    public static int getInt(String key){
        return configTree.getInt(key);
    }
    public static double getDouble(String key){
        return configTree.getDouble(key);
    }
    public static String getString(String key){
        return configTree.getString(key);
    }
    public static boolean getBoolean(String key){
        return configTree.getBoolean(key);
    }
    public static boolean contains(String key){
        return configTree.contains(key);
    }
}
