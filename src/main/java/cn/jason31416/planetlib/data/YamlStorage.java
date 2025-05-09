package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.PlanetLib;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class YamlStorage extends AbstractStorage {
    File directory;
    public YamlStorage(String version, File directory) {
        super(version);
        this.directory = directory;
    }
    @Override
    public void save() {
        for(DataList<?> dataList : dataLists){
            File file = new File(directory, dataList.getName() + ".yml");
            YamlConfiguration config = new YamlConfiguration();
            config.set("__META.timestamp", System.currentTimeMillis());
            config.set("__META.version", version);
            for(Object data : dataList.getAllData()){
                DataItem dataItem = new DataItem();
                try {
                    if (dataList.serialize(data, dataItem)) {
                        for (String key : dataItem.data.keySet()) {
                            config.set(dataItem.getUUID() + "." + key, dataItem.data.get(key));
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[PlanetLib] Failed to save data: "+directory.getName()+"!");
                    e.printStackTrace();
                }
            }
            try {
                config.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().warning("[PlanetLib] Failed to save data: "+directory.getName()+"!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void load() {
        for(DataList<?> dataList : dataLists){
            File file = new File(directory, dataList.getName() + ".yml");
            if(file.exists()) {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                if(config.contains("__META.version")&&!Objects.equals(config.get("__META.version"), version)){
                    try {
                        Bukkit.getLogger().warning("[PlanetLib] Noticed that data file "+dataList.getName()+".yml isn't saved in the same version!");
                        Bukkit.getLogger().warning("[PlanetLib] Backing up the file before loading (Can be found under)...");
                        File bakFolder = new File(PlanetLib.instance.getDataFolder(), "bak");
                        if((bakFolder.exists()||bakFolder.mkdir())&&bakFolder.isDirectory()){
                            config.save(new File(bakFolder, dataList.getName()+"-"+System.currentTimeMillis()+".yml"));
                        }
                    } catch (IOException e) {
                        Bukkit.getLogger().severe("Failed to backup the data!");
                    }
                }
                for (String key : config.getKeys(false)) {
                    if(key.equals("__META")) continue;
                    try {
                        ConfigurationSection section = config.getConfigurationSection(key);
                        if (section == null) continue;
                        DataItem dataItem = new DataItem();
                        dataItem.setUUID(UUID.fromString(key));
                        for (String subKey : section.getKeys(false)) {
                            dataItem.put(subKey, section.get(subKey));
                        }
                        dataList.deserialize(dataItem);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("[PlanetLib] Failed to load data: "+directory.getName()+"!");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
