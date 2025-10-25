package cn.jason31416.planetlib.gui.itemgroup;

import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUIRunnable;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import lombok.Setter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * InventoryItem is a group of items that are exactly the same (e.g. behaves and looks exactly like each other)
 */
public class InventoryList implements InventoryComponent {
    public static record ListItem(Provider<SimpleItemStack> item, @Nullable List<GUIRunnable> clickHandlers){}

    public List<Integer> slots;

    public Map<String, Provider<String>> placeholders = new HashMap<>();

    private final List<ListItem> listItems;

    public int page=0;

    @Setter
    private SimpleItemStack filler=null;

    public InventoryList(List<ListItem> listItems, List<Integer> slots) {
        this.slots = slots;
        this.listItems = listItems;
    }

    /**
     * adding a placeholder for dynamic replacements during refreshes.
     */
    public void addPlaceholder(String placeholder, Provider<String> value) {
        placeholders.put(placeholder, value);
    }

    /**
     * create a copy of the current InventoryItem instance
     */
    public InventoryList copy() {
        return new InventoryList(new ArrayList<>(listItems), new ArrayList<>(slots));
    }

    public int getPageCount() {
        return (int) Math.ceil((double) listItems.size() / slots.size());
    }

    public void nextPage() {
        page = (page + 1) % getPageCount();
    }
    public void previousPage() {
        page = (page - 1 + getPageCount()) % getPageCount();
    }

    @Override
    public void apply(GUI gui, Inventory inventory, Map<Integer, List<GUIRunnable>> clickHandlers) {
        int index = page * slots.size();
        for (int slot : slots) {
            if(index >= listItems.size()){
                if(filler != null)
                    inventory.setItem(slot, InventoryItem.putNbt(filler.toBukkitItem()));
                continue;
            }
            ListItem pair = listItems.get(index);
            SimpleItemStack itemstack = pair.item.get();
            for (Map.Entry<String, Provider<String>> entry : placeholders.entrySet()) {
                itemstack.placeholder(entry.getKey(), entry.getValue().get());
            }
            itemstack.papi(gui.getPlayer());
            inventory.setItem(slot, InventoryItem.putNbt(itemstack.toBukkitItem()));
            if(pair.clickHandlers!=null)
                clickHandlers.put(slot, pair.clickHandlers);
            index++;
        }
    }

    @Override
    public List<Integer> getSlots() {
        return slots;
    }

    @Override
    public void removeSlot(int slot) {
        slots.remove(Integer.valueOf(slot));
    }
}
