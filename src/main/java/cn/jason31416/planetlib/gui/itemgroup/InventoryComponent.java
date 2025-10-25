package cn.jason31416.planetlib.gui.itemgroup;

import cn.jason31416.planetlib.gui.GUI;
import cn.jason31416.planetlib.gui.GUIRunnable;
import org.bukkit.inventory.Inventory;

import java.util.List;
import java.util.Map;

public interface InventoryComponent {
    void apply(GUI gui, Inventory inventory, Map<Integer, List<GUIRunnable>> clickers);
    List<Integer> getSlots();
    void removeSlot(int slot);
    InventoryComponent copy();
}
