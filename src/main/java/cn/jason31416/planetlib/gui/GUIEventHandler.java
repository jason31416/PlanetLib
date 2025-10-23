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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Objects;

@ApiStatus.Internal
public class GUIEventHandler implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if(event.getClickedInventory() == null) return;
        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();
        SimplePlayer player = SimplePlayer.of((Player) event.getWhoClicked());
        if(GUISession.getSessions().containsKey(player) && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY){
            event.setCancelled(true);
            return;
        }
        if(!(inventory.getHolder() instanceof GUI gui)){
            if (NbtHook.hasTag(itemStack, "plib.guiItem")) {
                event.getClickedInventory().remove(Objects.requireNonNull(event.getCurrentItem()));
                StaticMessages.UNKNOWN_GUI_ITEM.sendConsole();
            }
            return;
        }
        GUIRunnable.RunnableInvocation invocation = new GUIRunnable.RunnableInvocation(gui, event, false);
        gui.getClickHandlers().getOrDefault(event.getSlot(), List.of())
                .forEach(handler -> handler.run(invocation));
        event.setCancelled(!invocation.allow);
    }
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event){
        if (event.getInventory().getHolder() instanceof GUI gui) {
            GUISession.getSessions().remove(gui.getPlayer());
        }
    }

}
