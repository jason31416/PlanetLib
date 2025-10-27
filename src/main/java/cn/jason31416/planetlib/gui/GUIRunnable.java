package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface GUIRunnable {
    @Getter @AllArgsConstructor
    class RunnableInvocation{
        public GUI gui;
        public InventoryClickEvent event;
        public boolean allow;
        public GUISession getSession(){
            return gui.getSession();
        }
        public SimplePlayer getPlayer(){
            return gui.getPlayer();
        }
        public int getSlot(){
            return event.getSlot();
        }
        public InventoryAction getAction(){
            return event.getAction();
        }
    }
    void run(RunnableInvocation invocation);
}