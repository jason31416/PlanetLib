package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.gui.itemgroup.InventoryComponent;
import cn.jason31416.planetlib.gui.itemgroup.InventoryItem;
import cn.jason31416.planetlib.gui.itemgroup.InventoryList;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * GUI builder
 * A second way to create GUIs
 * Can be used to create temp. GUIs that doesn't need to be configured.
 *
 * @since 1.3
 */
public class GUIBuilder {
    public interface ItemBuilder {
        InventoryComponent build(List<Integer> slots);
        String getId();
    }

    public static class StackedItem implements ItemBuilder {
        String id=null;
        Provider<SimpleItemStack> item;
        List<GUIRunnable> runnables=new ArrayList<>();

        public static StackedItem builder(){
            return new StackedItem();
        }
        public StackedItem id(String id){
            this.id=id;
            return this;
        }
        public StackedItem item(Provider<SimpleItemStack> item){
            this.item=item;
            return this;
        }
        public StackedItem runnable(GUIRunnable runnable){
            runnables.add(runnable);
            return this;
        }

        @Override
        public InventoryComponent build(List<Integer> slots) {
            return new InventoryItem(item, slots, runnables);
        }
        @Override
        public String getId() {
            return id;
        }
    }
    public static class ListedItem implements ItemBuilder {
        String id=null;
        List<InventoryList.ListItem> listItems;

        public static ListedItem builder(){
            return new ListedItem();
        }
        public ListedItem id(String id){
            this.id=id;
            return this;
        }
        public ListedItem items(List<InventoryList.ListItem> items){
            if(listItems==null) listItems=new ArrayList<>();
            listItems.addAll(items);
            return this;
        }

        @Override
        public InventoryComponent build(List<Integer> slots) {
            return new InventoryList(listItems, slots);
        }

        @Override
        public String getId() {
            return id;
        }
    }

    private Message name;
    private String shape;
    private final String id;
    private final Map<String, ItemBuilder> items = new HashMap<>();

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
        items.put(key, StackedItem.builder().id(id).item(() -> null));
        return this;
    }
    public GUIBuilder setItem(String key, ItemBuilder item){
        items.put(key, item);
        return this;
    }
    public GUIBuilder setItem(String key, SimpleItemStack item){
        items.put(key, StackedItem.builder().id(key).item(() -> item));
        return this;
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
            String id = items.get(c).getId();
            if(id == null) id = c;
            List<Integer> slots = new ArrayList<>();
            for (int i = 0; i < shapeArr.length; i++) {
                if(shapeArr[i].equals("-")) continue;
                if(shapeArr[i].equals(c)){
                    slots.add(i);
                }
            }
            ret.addItem(id, items.get(c).build(slots));
        }
        return ret;
    }
}
