package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.util.MapTree;
import cn.jason31416.planetlib.util.Util;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class MessageTheme {
    @Getter
    private static Map<String, MessageTheme> themes = new HashMap<>();

    private final Map<String, String> colors;

    public MessageTheme(MapTree tree) {
        this.colors = new HashMap<>();
        for (String key : tree.getKeys()) {
            String value = tree.getString(key);
            if (!value.isEmpty()) {
                colors.put(key, value);
            }
        }
    }

    private MessageTheme(Map<String, String> colors) {
        this.colors = new HashMap<>(colors);
    }

    public String applyTheme(String text) {
        for (Map.Entry<String, String> entry : colors.entrySet()) {
            text = text.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        return text;
    }

    public MessageTheme copy() {
        return new MessageTheme(colors);
    }

    public static void loadThemesFromFile(String path) {
        Util.savePluginResource(path);
        MapTree tree;
        try {
            tree = MapTree.fromYaml(Files.readString(new File(PlanetLib.instance.getDataFolder(), path).toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(String i: tree.getKeys()){
            if(tree.getSection(i) == null) continue;
            themes.put(i, new MessageTheme(tree.getSection(i)));
        }
    }

    public static void useTheme(String themeID){
        Message.useTheme(themeID);
    }
}
