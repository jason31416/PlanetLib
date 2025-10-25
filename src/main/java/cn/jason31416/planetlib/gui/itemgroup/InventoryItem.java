package cn.jason31416.planetlib.gui.itemgroup;

import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUIRunnable;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * InventoryItem is a group of items that are exactly the same (e.g. behaves and looks exactly like each other)
 */
public class InventoryItem implements InventoryComponent {
    /**
     * SimpleItemStack provider
     */
    public Provider<SimpleItemStack> stack;

    /**
     * Item slot
     */
    public List<Integer> slots;

    /**
     * For dynamic replacements during refreshes
     */
    public Map<String, Provider<String>> placeholders = new HashMap<>();

    /**
     * Click event handlers.
     */
    public List<GUIRunnable> clickable;

    /**
     * Creating a new InventoryItem instance
     *
     * @param stack     SimpleItemStack provider (Called every refresh)
     * @param slots     The slots that this item will be placed in
     * @param clickable click event handlers
     */
    public InventoryItem(Provider<SimpleItemStack> stack, List<Integer> slots, List<GUIRunnable> clickable) {
        this.stack = stack;
        this.slots = slots;
        this.clickable = clickable;
    }

    /**
     * Creating a new InventoryItem instance
     *
     * @param stack SimpleItemStack provider (Called every refresh)
     * @param slots The slots that this item will be placed in
     * @return new InventoryItem instance
     */
    public static InventoryItem create(Provider<SimpleItemStack> stack, List<Integer> slots) {
        return new InventoryItem(stack, slots, new ArrayList<>());
    }

    /**
     * Creating a new InventoryItem instance via static item
     *
     * @param stack SimpleItemStack instance
     * @param slots The slots that this item will be placed in
     * @return new InventoryItem instance
     */
    public static InventoryItem create(SimpleItemStack stack, List<Integer> slots) {
        return new InventoryItem(stack::copy, slots, new ArrayList<>());
    }

    /**
     * Adding a dynamic item modifier that will be called every time the item is refreshed.
     */
    public InventoryItem addItemModifier(Consumer<SimpleItemStack> modifier) {
        stack = () -> {
            SimpleItemStack s = stack.get();
            modifier.accept(s);
            return s;
        };
        return this;
    }

    /**
     * Adding a click event handler to the item.
     */
    public InventoryItem addClickHandler(GUIRunnable runnable) {
        clickable.add(runnable);
        return this;
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
    public InventoryItem copy() {
        return new InventoryItem(stack, new ArrayList<>(slots), new ArrayList<>(clickable));
    }

    public static ItemStack putNbt(ItemStack item){
        NbtHook.addTag(item, "plib.guiItem");
        return item;
    }

    @Override
    public void apply(GUI gui, Inventory inventory, Map<Integer, List<GUIRunnable>> clickHandlers) {
        ItemStack bstack=null;
        SimpleItemStack stack = this.stack.get();
        if(stack != null) {
            for (String p : placeholders.keySet()) {
                stack.placeholder(p, placeholders.get(p).get());
            }
            stack.papi(gui.getPlayer());
            bstack = putNbt(stack.toBukkitItem());
        }
        List<GUIRunnable> clonedRunnables = new ArrayList<>(clickable);
        for (int slot : slots) {
            if(bstack != null)
                inventory.setItem(slot, bstack.clone());
            clickHandlers.put(slot, clonedRunnables);
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
