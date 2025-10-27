package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.gui.itemgroup.InventoryComponent;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.util.PluginLogger;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
    private final Map<String, InventoryComponent> content = new HashMap<>();

    private final Map<Integer, Pair<String, InventoryComponent>> slotMapping = new HashMap<>();

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
     * Constructor of GUI
     *
     * @param size Size of GUI (Can be multiples of 9 or less than 9)
     * @param title The title of the GUI
     */
    public GUI(String id, int size, Message title){
        inventory = Bukkit.getServer().createInventory(this, size, title.toComponent());
        this.id = id;
    }

    /**
     * Setting the session of the GUI. This is used to get the player instance.
     * @param session The session that requested this GUI
     *
     * @return this GUI instance
     * @since 1.3.1
     */
    public GUI setSession(GUISession session){
        this.session = session;
        return this;
    }

    public void onClose(Runnable onCloseRunnable){
        this.onCloseRunnable = onCloseRunnable;
    }

    /**
     * Setting the session & displaying the GUI.
     * @param session The session that requested this GUI
     */
    public void display(GUISession session){
        setSession(session);
        display();
    }

    /**
     * Fetching an item by its key.
     */
    @Nullable
    public InventoryComponent getItem(String key){
        return content.get(key);
    }

    /**
     * Fetching an item by its slot.
     */
    @Nullable
    public InventoryComponent getItem(int slot){
        return slotMapping.get(slot).second();
    }

    public void addItem(String itemId, InventoryComponent item){
        new ArrayList<>(item.getSlots()).forEach(slot -> {
            if (slotMapping.containsKey(slot)) {
                slotMapping.get(slot).second().getSlots().remove(slot);
                if (slotMapping.get(slot).second().getSlots().isEmpty()) {
                    content.remove(slotMapping.get(slot).first());
                }
            }
            slotMapping.put(slot, Pair.of(itemId, item));
        });
        content.put(itemId, item);
    }

    public void removeItem(String itemId){
        InventoryComponent item = content.remove(itemId);
        if(item!= null) {
            item.getSlots().forEach(slotMapping::remove);
        }
    }
    public void removeItem(int slot){
        Pair<String, InventoryComponent> pair = slotMapping.remove(slot);
        if(pair!= null) {
            pair.second().removeSlot(slot);
            if(pair.second().getSlots().isEmpty())
                content.remove(pair.first());
        }
    }

    /**
     * Display the GUI to a session. Ideally should only be called by the session.
     */
    public void display(){
        if(session==null) throw new IllegalStateException("Session is not set.");
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
        if(session==null) throw new IllegalStateException("Session is not set.");
        inventory.clear();
        clickHandlers.clear();
        for (var entry : content.entrySet()) {
            InventoryComponent item = entry.getValue();
            item.apply(this, inventory, clickHandlers);
        }
    }
}
