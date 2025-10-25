package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.gui.clickaction.RegisteredGUIRunnable;
import cn.jason31416.planetlib.gui.itemgroup.InventoryItem;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.util.MapTree;
import cn.jason31416.planetlib.util.PluginLogger;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.util.general.ShitMountainException;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.*;

public class GUITemplate {
    public static Map<String, GUITemplate> loadedTemplates = new HashMap<>();

    public Message name;
    public String id;
    public int size;
    public int refreshInterval=-1;
    public String base=null;
    public Map<String, InventoryItem> inventory = new HashMap<>();

    public static GUI getGUI(String id) {
        GUITemplate template = loadedTemplates.get(id);
        if (template == null) {
            throw new IllegalArgumentException("GUI template not found: " + id);
        }
        return template.createGUI();
    }
    public static GUITemplate getTemplate(String id) {
        GUITemplate template = loadedTemplates.get(id);
        if (template == null) {
            throw new IllegalArgumentException("GUI template not found: " + id);
        }
        return template;
    }
    public GUITemplate(Message name) {
        this.name = name;
    }

    public GUI createGUI() {
        GUI gui = new GUI(id, size, name);
        gui.refresh(refreshInterval);
        GUITemplate curBase = this;
        Set<String> usedBases = new HashSet<>();
        Stack<GUITemplate> stack = new Stack<>();
        while(true){
            stack.push(curBase);

            if(curBase.base!=null&&!loadedTemplates.containsKey(curBase.base)){
                PluginLogger.error("GUI template not found: "+curBase);
                break;
            }else if(curBase.base==null) break;
            if(usedBases.contains(curBase.base)){
                PluginLogger.error("Circular reference found in GUI template: "+curBase);
                break;
            }
            usedBases.add(curBase.base);
            curBase = loadedTemplates.get(curBase.base);
            if(curBase.size!=size){
                throw new IllegalArgumentException("GUI template size for "+name.toString()+" mismatch with base: "+curBase);
            }
        }
        while(!stack.empty()){
            curBase = stack.pop();
            for(String key: curBase.inventory.keySet()) {
                gui.addItem(key, curBase.inventory.get(key));
            }
        }
        return gui;
    }

    public static GUITemplate loadFromMapTree(String id, MapTree section){
        GUITemplate template = new GUITemplate(Message.of(section.getString("name")));
        template.id = id;
        template.refreshInterval = section.getInt("refresh", -1);
        if (!section.contains("shape"))
            throw new IllegalArgumentException("GUI template section must contain 'shape' key");
        String[] shape = String.join(" ", section.getStringList("shape")).split(" ");
        template.size = shape.length;
        Map<String, Pair<String, Provider<SimpleItemStack>>> items = new HashMap<>();
        Map<String, List<GUIRunnable>> clickActions = new HashMap<>();

        if(section.contains("base")){
            template.base = section.getString("base");
        }

        for (String key : section.getSection("items").getKeys()) {
            MapTree item = section.getSection("items." + key);
            SimpleItemStack stack = new SimpleItemStack();
            String itemID;
            if (item.contains("id")) {
                itemID = item.getString("id");
            } else {
                itemID = key;
            }
            if (item.contains("material"))
                stack.setMaterial(Material.getMaterial(item.getString("material").toUpperCase(Locale.ROOT)));
            else if (!item.contains("skull")) {
                items.put(key, Pair.of(itemID, () -> null));
                continue;
            }
            if (item.contains("amount"))
                stack.setQuantity(item.getInt("amount"));
            if (item.contains("name"))
                stack.setName(Message.of(item.getString("name")));
            if (item.contains("lore"))
                stack.setLore(item.getStringList("lore"));
            if (item.contains("glow"))
                stack.setGlow(item.getBoolean("glow"));
            else if (item.contains("enchantments")) {
                stack.enchantments = new HashMap<>();
                for (String enchantment : item.getSection("enchantments").getKeys()) {
                    if (Enchantment.getByName(enchantment) != null) {
                        stack.enchantments.put(Enchantment.getByName(enchantment.toUpperCase(Locale.ROOT)), item.getInt("enchantments." + enchantment));
                    } else {
                        throw new IllegalArgumentException("Invalid enchantment for item: " + enchantment);
                    }
                }
            }
            if (item.contains("model")) {
                stack.setCustomModelData(item.getInt("model"));
            } else if (item.contains("custom-model-data")) {
                stack.setCustomModelData(item.getInt("custom-model-data"));
            }
            if (item.contains("skull")) {
                stack.setSkullID(item.getString("skull"));
                stack.setMaterial(Material.PLAYER_HEAD);
            }
            if (item.contains("click")) {
                for (String i : item.getStringList("click")) {
                    String[] parts = i.split(" ");
                    if (RegisteredGUIRunnable.getClickHandlers().containsKey(parts[0])) {
                        GUIRunnable runnable = RegisteredGUIRunnable.getClickHandlers().get(parts[0]).apply(parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0]);
                        clickActions.computeIfAbsent(key, k -> new ArrayList<>()).add(runnable);
                    } else throw new IllegalArgumentException("Invalid click action: " + parts[0]);
                }
            }
            items.put(key, Pair.of(itemID, stack::copy));
        }
        for (String i : shape) {
            if (i.equals("-")) continue;
            if (!items.containsKey(i)) {
                throw new IllegalArgumentException("Invalid item ID: " + i);
            }
        }
        for (String item : items.keySet()) {
            Pair<String, Provider<SimpleItemStack>> pair = items.get(item);
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < shape.length; i++) {
                if (shape[i].equals(item)) {
                    slots.add(i);
                }
            }
            InventoryItem invitem = new InventoryItem(
                    pair.second(),
                    slots,
                    clickActions.getOrDefault(item, new ArrayList<>())
            );
            template.inventory.put(pair.first(), invitem);
        }
        loadedTemplates.put(id, template);
        return template;
    }
    public static void loadFromFile(File file){
        try(FileInputStream fis = new FileInputStream(file)){
            MapTree tree = MapTree.fromYaml(new String(fis.readAllBytes()));
            loadFromMapTree(file.getName().substring(0, file.getName().lastIndexOf('.')), tree);
        }catch (Exception e){
            e.printStackTrace();
            throw new ShitMountainException("Failed to load GUI template from file: "+file.getName(), e);
        }
    }
    public static void loadFromDirectory(File directory){
        for(File file: directory.listFiles()){
            if(file.isFile() && (file.getName().endsWith(".yml")||file.getName().endsWith(".yaml"))){
                loadFromFile(file);
            }
        }
    }
    public static void clearLoaded(){
        loadedTemplates.clear();
    }
    public void saveToFile(File file){
        try(FileWriter fw = new FileWriter(file)){
            MapTree tree = new MapTree();
            tree.put("name", name.toString())
                    .put("refresh-interval", refreshInterval);
            String[] shape = new String[size];
            Arrays.fill(shape, "-");
            MapTree items = new MapTree();
            String availableKeys = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"; // This is enough for a 54-slot inventory already.
            int keyIdx=0;
            for(String key: inventory.keySet()) {
                InventoryItem item = inventory.get(key);
                MapTree itemTree = new MapTree();
                itemTree.put("id", key);

                SimpleItemStack stack = item.stack.get();
                if(stack != null){
                    itemTree.put("material", stack.material.name().toLowerCase(Locale.ROOT));
                    itemTree.put("amount", stack.quantity);
                    itemTree.put("name", stack.name.toFormatted());
                    if(stack.lore!= null)
                        itemTree.put("lore", stack.lore.asList());
                    if(stack.glow)
                        itemTree.put("glow", true);
                    if(stack.customModelData!=-1)
                        itemTree.put("model", stack.customModelData);
                    if(stack.enchantments!= null){
                        for(Enchantment enchantment: stack.enchantments.keySet()){
                            itemTree.put("enchantments."+enchantment.getName(), stack.enchantments.get(enchantment));
                        }
                    }
                    if(stack.skullId!= null) {
                        itemTree.put("skull", stack.skullId);
                    }
                }

                String itemKey = ""+availableKeys.charAt(keyIdx++);
                for(int slot: item.slots){
                    shape[slot] = itemKey;
                }
                items.put(itemKey, itemTree.data);
            }
            tree.put("shape", shape)
                    .put("items", items.data);
            fw.write(tree.toYaml());
        }catch (Exception e){
            throw new ShitMountainException("Failed to save GUI template to file: "+file.getName(), e);
        }
    }
}
