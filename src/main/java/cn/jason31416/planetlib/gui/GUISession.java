package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;

public abstract class GUISession {
    public static final Map<SimplePlayer, GUISession> sessions = new java.util.HashMap<>();
    public final SimplePlayer player;
    public GUI gui=null;
    public GUISession(SimplePlayer player) {
        this.player = player;
        if(sessions.containsKey(player)) sessions.get(player).close();
        sessions.put(player, this);
    }
    public abstract void constructGUI(String guiID, GUI gui);
    public void display(GUI gui) {
        if(gui == null) throw new IllegalArgumentException("GUI cannot be null");
        this.gui = gui;
        gui.display(player);
    }
    public void handleClick(int slot, InventoryAction action, InventoryClickEvent event) {
        gui.handleClick(slot, this, action, event);
    }
    public void display(String guiID){
        try {
            if (!GUILoader.loadedGUIs.containsKey(guiID)) {
                player.sendMessage(new StringMessage("<red>Error: Missing GUI "+guiID+", please contact admin!"));
                player.getPlayer().closeInventory();
                PlanetLib.instance.getLogger().severe("Error: Missing GUI: "+guiID+"!");
                return;
            }
            GUI gui = GUILoader.getGUI(guiID);
            constructGUI(guiID, gui);
            display(gui);
        } catch (Exception e) {
            player.sendMessage(new StringMessage("<red>Error: Some Error has occured when loading GUI: "+guiID+", please contact admin!"));
            PlanetLib.instance.getLogger().severe("Error while loading GUI: "+guiID+"!");
            e.printStackTrace();
        }
    }
    public void close() {
        sessions.remove(player);
        player.getPlayer().closeInventory();
    }
    public Inventory getCurrentInventory() {
        return gui.lstInventory;
    }
}
