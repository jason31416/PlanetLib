package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class GUILoader {
    public static Map<String, GUI> loadedGUIs = new HashMap<>();
    private static GUI loadFromConfig(ConfigurationSection config){
        GUI gui = new GUI();
        ConfigurationSection windowSettings = Objects.requireNonNull(config.getConfigurationSection("window"), "Incomplete config structure!");
        gui.title = windowSettings.getString("title", "");
        gui.size = windowSettings.getInt("size");
        if(windowSettings.contains("inputs")){
            gui.inputs = new HashSet<>(windowSettings.getIntegerList("inputs"));
        }
        ConfigurationSection items = Objects.requireNonNull(config.getConfigurationSection("container"), "Incomplete config structure!");
        for(String key : items.getKeys(false)) {
            ConfigurationSection item = Objects.requireNonNull(items.getConfigurationSection(key), "Incomplete config structure!");
            List<String> lore = new ArrayList<>();
            if(item.contains("lore")) for(String line : item.getStringList("lore")){
                lore.add(new StringMessage(line).toString());
            }
            Material mat = Material.valueOf(item.getString("material", "AIR"));
            if(item.contains("skull")) mat = Material.PLAYER_HEAD;
            if(item.contains("slot")){
                GUI.Item guiItem = gui.addItem(key, new StringMessage(item.getString("name", "")).toString(), item.getInt("slot"), mat, item.getInt("amount", 1));
                if(!lore.isEmpty()) guiItem.setLore(lore);
                if(item.contains("model")) guiItem.setCustomModelData(item.getInt("model"));
                if(item.contains("skull")) guiItem.setSkullID(item.getString("skull"));
                if(item.getBoolean("glow", false)) guiItem.setGlow(true);
            } else {
                List<GUI.Item> guiItems = gui.addItem(key, new StringMessage(item.getString("name", "")).toString(), item.getIntegerList("slots"), mat, item.getInt("amount", 1));
                if(!lore.isEmpty()) for(GUI.Item guiItem : guiItems) guiItem.setLore(lore);
                if(item.contains("model")) for(GUI.Item guiItem : guiItems) guiItem.setCustomModelData(item.getInt("model"));
                if(item.contains("skull")) for(GUI.Item guiItem : guiItems) guiItem.setSkullID(item.getString("skull"));
                if(item.getBoolean("glow", false)) for(GUI.Item guiItem : guiItems) guiItem.setGlow(true);
            }
        }
        return gui;
    }
    public static void loadFile(File path){
        YamlConfiguration config = YamlConfiguration.loadConfiguration(path);
        for(String key : config.getKeys(false)){
            try{
                loadedGUIs.put(key, loadFromConfig(Objects.requireNonNull(config.getConfigurationSection(key))));
            }catch (Exception e){
                StaticMessages.ERROR_GUI_CONFIG.add("file", path.getName()).add("key", key).sendConsole();
                e.printStackTrace();
            }
        }
    }
    public static GUI getGUI(String name){
        return loadedGUIs.get(name).copy();
    }
}
