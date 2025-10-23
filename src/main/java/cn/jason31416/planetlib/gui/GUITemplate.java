package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.message.Message;

import java.util.*;

public class GUITemplate {
    public Message name;
    public String id;
    public int size;
    public int refreshInterval=-1;
    public Map<String, GUI.InventoryItem> inventory = new HashMap<>();

    public GUITemplate(Message name) {
        this.name = name;
    }

    public GUI createGUI() {
        GUI gui = new GUI(id, size, name);
        gui.refresh(refreshInterval);
        gui.getContent().putAll(inventory);
        return gui;
    }
}
