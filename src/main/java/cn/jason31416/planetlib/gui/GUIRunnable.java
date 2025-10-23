package cn.jason31416.planetlib.gui;

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
        public InventoryAction getAction(){
            return event.getAction();
        }
    }
    void run(RunnableInvocation invocation);
}