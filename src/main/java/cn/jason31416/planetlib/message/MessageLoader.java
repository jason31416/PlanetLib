package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.PlanetLib;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MessageLoader {
    public ConfigurationSection messageConfig;
    public MessageLoader(ConfigurationSection messageConfig) {
        this.messageConfig = messageConfig;
    }
    public MessageLoader(File filePath) {
        try{
            this.messageConfig = YamlConfiguration.loadConfiguration(filePath);
        }catch (Exception ignored){
            throw new RuntimeException("Failed to load message config file!");
        }
    }
    public StringMessage getMessage(String key, String defaultMessage) {
        if(messageConfig.isList(key)){
            return new StringMessage(String.join("\n", messageConfig.getStringList(key)));
        }
        if(messageConfig.contains(key)) return new StringMessage(messageConfig.getString(key));
        return defaultMessage == null? null : new StringMessage(defaultMessage);
    }
    public String getRawMessage(String key, String defaultMessage) {
        if(messageConfig.contains(key)) return messageConfig.getString(key);
        return defaultMessage;
    }
    public List<String> getList(String key, List<String> defaultList) {
        if(messageConfig.isList(key)) return messageConfig.getStringList(key);
        return defaultList;
    }
}
