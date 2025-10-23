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
import java.util.function.Consumer;

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
    private final String id;
    private final Map<String, Pair<String, Provider<SimpleItemStack>>> items = new HashMap<>();

    public GUIBuilder(String id) {
        this.id = id;
    }
    public GUIBuilder name(Message name) {
        this.name = name;
        return this;
    }
    public GUIBuilder shape(String shape) {
        this.shape = shape;
        return this;
    }
    public GUIBuilder shape(String... shape) {
        this.shape = String.join(" ", shape);
        return this;
    }
    public GUIBuilder shape(List<String> shape){
        this.shape = String.join(" ", shape);
        return this;
    }
    public GUIBuilder setItem(String key, String id){
        items.put(key, Pair.of(id, ()->null));
        return this;
    }
    public GUIBuilder setItem(String key, String id, Provider<SimpleItemStack> item){
        items.put(key, Pair.of(id, item));
        return this;
    }
    public GUIBuilder setItem(String key, String id, SimpleItemStack item){
        return setItem(key, id, item::copy);
    }
    public GUIBuilder setItem(String key, SimpleItemStack item){
        return setItem(key, ""+key, item::copy);
    }
    public GUIBuilder setItem(String key, Material material, int amount, Message name){
        return setItem(key, new SimpleItemStack().setMaterial(material).setQuantity(amount).setName(name));
    }

    public GUI build() {
        String[] shapeArr = shape.split(" ");
        GUI ret = new GUI(id, shapeArr.length, name);
        for (int i = 0; i < shapeArr.length; i++) {
            if(shapeArr[i].equals("-")) continue;
            if(!items.containsKey(shapeArr[i]))
                throw new IllegalArgumentException("No item provider for key: " + shapeArr[i]);
        }
        for(String c: items.keySet()){
            String id = items.get(c).first();
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < shapeArr.length; i++) {
                if(shapeArr[i].equals("-")) continue;
                if(shapeArr[i].equals(c)){
                    slots.add(i);
                }
            }
            ret.getContent().put(id, new GUI.InventoryItem(items.get(c).second(), slots, new ArrayList<>()));
        }
        return ret;
    }
}
