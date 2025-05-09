package cn.jason31416.planetlib.gui;


import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class GUIEventHandler implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        ItemStack itemStack = event.getCurrentItem();
        SimplePlayer player = SimplePlayer.of((Player) event.getWhoClicked());
        if(GUISession.sessions.containsKey(player)&&(
                event.getClickedInventory().equals(GUISession.sessions.get(player).getCurrentInventory())||
                event.getAction()==InventoryAction.MOVE_TO_OTHER_INVENTORY
        )){
            event.setCancelled(true);
        }
        if (NbtHook.hasTag(itemStack, "bn.guiItem")) {
            event.setCancelled(true);
            if(GUISession.sessions.containsKey(player)&&event.getClickedInventory().equals(GUISession.sessions.get(player).getCurrentInventory())){
                GUISession session = GUISession.sessions.get(player);
                session.handleClick(event.getSlot(), event.getAction(), event);
            }else{
                event.getClickedInventory().remove(event.getCurrentItem());
                StaticMessages.UNKNOWN_GUI_ITEM.sendConsole();
            }
        }
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        SimplePlayer player = SimplePlayer.of((event.getPlayer()));
        if(GUISession.sessions.containsKey(player)) {
            if (GUISession.sessions.get(player).gui == null || GUISession.sessions.get(player).gui.lstInventory == event.getInventory()){
                GUISession.sessions.remove(player);
            }
        }
    }

}
