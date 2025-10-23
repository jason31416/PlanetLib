package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.util.PluginLogger;
import cn.jason31416.planetlib.util.general.Provider;
import cn.jason31416.planetlib.wrapper.SimpleItemStack;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * GUI class is essentially a wrapper of Bukkit's Inventory class.
 * A new GUI will be created every time a session requests one.
 * It also handles refreshing logics.
 *
 * @since 1.3
 */
public class GUI implements InventoryHolder {
    /**
     * Bukkit inventory instance of the GUI
     */
    @Getter
    private final Inventory inventory;
    
    /**
     * Mapping for the click handlers of the GUI, key is the slot index, value is a list of click handlers
     */
    @Getter
    private final Map<Integer, List<GUIRunnable>> clickHandlers = new HashMap<>();
    
    /**
     * GUI content mapping
     */
    @Getter
    private final Map<String, InventoryItem> content = new HashMap<>();

    @Getter
    private final String id;

    @Getter
    private int refreshInterval=-1;

    private Runnable onCloseRunnable = null;

    /**
     * The session that requested this GUI
     */
    @Getter
    private GUISession session;

    private WrappedTask refreshTask=null;

    /**
     * InventoryItem is a group of items that are exactly the same (e.g. behaves and looks exactly like each other)
     */
    public static class InventoryItem {
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
         * @param stack SimpleItemStack provider (Called every refresh)
         * @param slots The slots that this item will be placed in
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
         *
         * @return new InventoryItem instance
         */
        public static InventoryItem create(Provider<SimpleItemStack> stack, List<Integer> slots){
            return new InventoryItem(stack, slots, new ArrayList<>());
        }
        /**
         * Creating a new InventoryItem instance via static item
         *
         * @param stack SimpleItemStack instance
         * @param slots The slots that this item will be placed in
         *
         * @return new InventoryItem instance
         */
        public static InventoryItem create(SimpleItemStack stack, List<Integer> slots) {
            return new InventoryItem(stack::copy, slots, new ArrayList<>());
        }

        /**
         * Adding a dynamic item modifier that will be called every time the item is refreshed.
         */
        public InventoryItem addItemModifier(Consumer<SimpleItemStack> modifier){
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
        public InventoryItem addClickHandler(GUIRunnable runnable){
            clickable.add(runnable);
            return this;
        }

        /**
         * adding a placeholder for dynamic replacements during refreshes.
         */
        public void addPlaceholder(String placeholder, Provider<String> value){
            placeholders.put(placeholder, value);
        }

        /**
         * create a copy of the current InventoryItem instance
         */
        public InventoryItem copy() {
            return new InventoryItem(stack, new ArrayList<>(slots), new ArrayList<>(clickable));
        }
    }

    /**
     * Constructor of GUI
     *
     * @param size Size of GUI (Can be multiples of 9 or less than 9)
     * @param title The title of the GUI
     */
    public GUI(String id, int size, Message title){
        inventory = Bukkit.getServer().createInventory(this, size, title.toComponent());
        this.id = id;
    }

    public void onClose(Runnable onCloseRunnable){
        this.onCloseRunnable = onCloseRunnable;
    }

    /**
     * Display the GUI to a session. Ideally should only be called by the session.
     *
     * @param session The session that requested this GUI
     */
    public void display(GUISession session){
        this.session = session;
        session.player.getPlayer().openInventory(inventory);

        if(refreshInterval > 0 && refreshTask == null){
            refreshTask = PlanetLib.getScheduler().runTimer(()->{
                if(session.isClosed()||!getPlayer().isOnline()||getPlayer().getPlayer().getOpenInventory().getTopInventory() != inventory){
                    refreshTask.cancel();
                    PluginLogger.warning("Unexpected GUI refresh task cancelled due to session closed or player offline.");
                    return;
                }
                update();
            }, Ticks.duration(refreshInterval).toMillis(), Ticks.duration(refreshInterval).toMillis(), TimeUnit.MILLISECONDS);
        }
    }
    /**
     * Fetching the player instance of the GUI.
     */
    public SimplePlayer getPlayer(){
        return session.player;
    }

    /**
     * Setting the refreshing interval of the GUI (Must be set before display!).
     */
    public GUI refresh(int interval){
        this.refreshInterval = interval;
        return this;
    }

    /**
     * Fetching an item by its key.
     */
    public InventoryItem getItem(String key){
        return content.get(key);
    }

    /**
     * Fetching an item by its slot.
     */
    public InventoryItem getItem(int slot){
        for (var entry : content.entrySet()) {
            if(entry.getValue().slots.contains(slot)){
                return entry.getValue();
            }
        }
        return null;
    }

    private ItemStack putNbt(ItemStack item){
        NbtHook.addTag(item, "plib.guiItem");
        return item;
    }

    public void close(){
        _close();
        session.player.getPlayer().closeInventory();
    }

    @ApiStatus.Internal
    protected void _close(){
        if(onCloseRunnable != null){
            onCloseRunnable.run();
        }
        if(refreshTask != null){
            refreshTask.cancel();
        }
        if(session != null){
            session.close();
        }
    }

    /**
     * Refreshing the GUI.
     */
    public void update(){
        inventory.clear();
        clickHandlers.clear();
        for (var entry : content.entrySet()) {
            InventoryItem item = entry.getValue();
            ItemStack bstack=null;
            SimpleItemStack stack = item.stack.get().copy();
            if(stack != null) {
                for (String p : item.placeholders.keySet()) {
                    stack.placeholder(p, item.placeholders.get(p).get());
                }
                bstack = putNbt(stack.toBukkitItem());
            }
            List<Integer> slots = item.slots;
            List<GUIRunnable> clonedRunnables = new ArrayList<>(item.clickable);
            for (int slot : slots) {
                if(bstack != null)
                    inventory.setItem(slot, bstack.clone());
                clickHandlers.put(slot, clonedRunnables);
            }
        }
    }
}
