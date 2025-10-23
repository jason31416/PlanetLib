package cn.jason31416.planetlib.data;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.util.MapTree;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

public class JsonStorage extends AbstractStorage {
    File directory;
    public JsonStorage(String version, File directory) {
        super(version);
        this.directory = directory;
    }
    @Override
    public void save() {
        for(DataList<?> dataList : dataLists){
            File file = new File(directory, dataList.getName() + ".json");
            MapTree config = new MapTree();
            config.put("__META.timestamp", System.currentTimeMillis()).put("__META.version", version);
            for(Object data : dataList.getAllData()){
                DataItem dataItem = new DataItem();
                try {
                    if (dataList.serialize(data, dataItem)) {
                        for (String key : dataItem.data.keySet()) {
                            config.put(dataItem.getUUID() + "." + key, dataItem.data.get(key));
                        }
                    }
                } catch (Exception e) {
                    Bukkit.getLogger().warning("[PlanetLib] Failed to save data: "+directory.getName()+"!");
                    e.printStackTrace();
                }
            }
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(config.toJson());
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
                MapTree config;
                try (FileInputStream fis = new FileInputStream(file)){
                    config = MapTree.fromJson(new String(fis.readAllBytes()));
                }catch (IOException e){
                    Bukkit.getLogger().severe("Failed to load the data!");
                    return;
                }
                if(config.contains("__META.version")&&!Objects.equals(config.get("__META.version"), version)){
                    File bakFolder = new File(PlanetLib.instance.getDataFolder(), "bak");
                    if((bakFolder.exists()||bakFolder.mkdir())&&bakFolder.isDirectory()){
                        try (FileWriter fw = new FileWriter(new File(bakFolder, dataList.getName()+"-"+System.currentTimeMillis()+".yml"))) {
                            Bukkit.getLogger().warning("[PlanetLib] Noticed that data file "+dataList.getName()+".yml isn't saved in the same version!");
                            Bukkit.getLogger().warning("[PlanetLib] Backing up the file before loading (Can be found under)...");
                            fw.write(config.toJson());
                        } catch (IOException e) {
                            Bukkit.getLogger().severe("Failed to backup the data!");
                        }
                    }
                }
                for (String key : config.getKeys()) {
                    if(key.equals("__META")) continue;
                    try {
                        MapTree section = config.getSection(key);
                        if (section == null) continue;
                        DataItem dataItem = new DataItem();
                        dataItem.setUUID(UUID.fromString(key));
                        for (String subKey : section.getKeys()) {
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
