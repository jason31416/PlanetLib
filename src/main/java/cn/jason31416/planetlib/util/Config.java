package cn.jason31416.planetlib.util;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class Config {
    @Getter
    private static MapTree configTree;

    @SneakyThrows
    public static void load(JavaPlugin plugin) {
        Util.savePluginResource("config.yml");
        File configFile = new File(plugin.getDataFolder(), "config.yml");

        MapTree fileTree = MapTree.fromYaml(Files.readString(configFile.toPath()));
        configTree = fileTree;

        try (InputStream jarStream = plugin.getResource("config.yml")) {
            if (jarStream == null) {
                plugin.getLogger().warning("[PlanetLib] Default config.yml not found in jar resource; missing-key check skipped.");
                return;
            }

            String jarYaml = new String(jarStream.readAllBytes(), StandardCharsets.UTF_8);
            MapTree jarTree = MapTree.fromYaml(jarYaml);

            if (jarTree.contains("version")) {
                if (!jarTree.getString("version").equalsIgnoreCase(fileTree.getString("version"))) {
                    plugin.getLogger().warning("Detected configuration version is outdated, performing configuration update...");
                    String mergedYaml = mergeYamlPreserveCommentsAndOrder(Files.readString(configFile.toPath()), jarYaml);
                    Files.writeString(configFile.toPath(), mergedYaml, StandardCharsets.UTF_8);
                    configTree = MapTree.fromYaml(mergedYaml);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }
    }

    public static void start(JavaPlugin plugin) {
        load(plugin);
    }

    public static Object getItem(String key) {
        return configTree.get(key);
    }

    public static MapTree getSection(String key) {
        return configTree.getSection(key);
    }

    public static int getInt(String key) {
        return configTree.getInt(key);
    }

    public static double getDouble(String key) {
        return configTree.getDouble(key);
    }

    public static String getString(String key) {
        return configTree.getString(key);
    }

    public static boolean getBoolean(String key) {
        return configTree.getBoolean(key);
    }

    public static int getInt(String key, int def) {
        return configTree.getInt(key, def);
    }

    public static double getDouble(String key, double def) {
        return configTree.getDouble(key, def);
    }

    public static String getString(String key, String def) {
        return configTree.getString(key, def);
    }

    public static boolean getBoolean(String key, boolean def) {
        return configTree.getBoolean(key, def);
    }

    public static boolean contains(String key) {
        return configTree.contains(key);
    }

    private static String mergeYamlPreserveCommentsAndOrder(String fileYaml, String jarYaml) {
        try {
            YamlConfiguration fileConfig = new YamlConfiguration();
            fileConfig.options().parseComments(true);
            fileConfig.loadFromString(fileYaml);

            YamlConfiguration jarConfig = new YamlConfiguration();
            jarConfig.options().parseComments(true);
            jarConfig.loadFromString(jarYaml);

            // Use old file as template to preserve existing comments/format/order.
            // Only add keys introduced by new config, and force version to latest.
            for (String key : jarConfig.getKeys(true)) {
                Object jarVal = jarConfig.get(key);
                if (jarVal instanceof org.bukkit.configuration.ConfigurationSection) {
                    continue;
                }
                if ("version".equals(key)) {
                    continue;
                }
                if (!fileConfig.contains(key)) {
                    fileConfig.set(key, jarVal);
                    fileConfig.setComments(key, jarConfig.getComments(key));
                    fileConfig.setInlineComments(key, jarConfig.getInlineComments(key));
                }
            }

            if (jarConfig.contains("version")) {
                fileConfig.set("version", jarConfig.get("version"));
            }
            return fileConfig.saveToString();
        } catch (InvalidConfigurationException e) {
            throw new RuntimeException("Failed to merge config yaml while preserving comments/order", e);
        }
    }
}
