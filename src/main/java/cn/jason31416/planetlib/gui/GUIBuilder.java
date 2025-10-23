package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GUI builder
 * A second way to create GUIs
 * Can be used to create temp. GUIs that doesn't need to be configured.
 *
 * @since 1.3
 */
public class GUIBuilder {
    private Message name;
    private String shape;
    private final Map<Character, Pair<String, Provider<SimpleItemStack>>> items = new HashMap<>();

    public GUIBuilder name(Message name) {
        this.name = name;
        return this;
    }
    public GUIBuilder shape(String shape) {
        this.shape = shape;
        return this;
    }
    public GUIBuilder shape(List<String> shape){
        this.shape = String.join("", shape);
        return this;
    }
    public GUIBuilder setItem(char key, String id){
        items.put(key, Pair.of(id, ()->null));
        return this;
    }
    public GUIBuilder setItem(char key, String id, Provider<SimpleItemStack> item){
        items.put(key, Pair.of(id, item));
        return this;
    }
    public GUIBuilder setItem(char key, String id, SimpleItemStack item){
        return setItem(key, id, item::copy);
    }
    public GUIBuilder setItem(char key, SimpleItemStack item){
        return setItem(key, ""+key, item::copy);
    }
    public GUIBuilder setItem(char key, Material material, int amount, Message name){
        return setItem(key, new SimpleItemStack().setMaterial(material).setQuantity(amount).setName(name));
    }

    public GUITemplate build() {
        GUITemplate guiTemplate = new GUITemplate(name);
        guiTemplate.size = shape.length();
        for (int i = 0; i < shape.length(); i++) {
            if(!items.containsKey(shape.charAt(i)))
                throw new IllegalArgumentException("No item provider for key: " + shape.charAt(i));
        }
        for(Character c: items.keySet()){
            String id = items.get(c).first();
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < shape.length(); i++) {
                if(shape.charAt(i) == c){
                    slots.add(i);
                }
            }
            guiTemplate.inventory.put(id, new GUI.InventoryItem(items.get(c).second(), slots, new ArrayList<>()));
        }
        return guiTemplate;
    }
}
