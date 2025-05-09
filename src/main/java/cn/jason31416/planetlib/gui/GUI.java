package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.hook.NbtHook;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class GUI {
    public interface GUIRunnable {
        void run(GUISession session, InventoryAction action, InventoryClickEvent event);
    }
    public static class CommandRunnable implements GUIRunnable {
        public String command;
        public CommandRunnable(String command) {
            this.command = command;
        }
        public void run(GUISession session, InventoryAction action, InventoryClickEvent event) {
            Bukkit.dispatchCommand(session.player.getPlayer(), command);
        }
    }
    public static class SwitchGuiRunnable implements GUIRunnable {
        public String guiName;
        public SwitchGuiRunnable(String guiName) {
            this.guiName = guiName;
        }
        public void run(GUISession session, InventoryAction action, InventoryClickEvent event) {
            session.display(guiName);
        }
    }
    public static class CloseGuiRunnable implements GUIRunnable {
        public void run(GUISession session, InventoryAction action, InventoryClickEvent event) {
            session.close();
        }
    }
    public static class ItemGroup {
        List<Item> items=new ArrayList<>();
        public ItemGroup(List<Item> items){
            this.items=items;
        }
        @Deprecated
        public void setAsVanillaItemStack(ItemStack stack){
            for(Item item: items){
                item.setAsVanillaItemStack(stack);
            }
        }
        public ItemGroup setSkullID(String skullID){
            for(Item item: items){
                item.setSkullID(skullID);
            }
            return this;
        }
        public ItemGroup setItemStack(ItemStack stack){
            for(Item item: items){
                item.setItemStack(stack);
            }
            return this;
        }
        public ItemGroup setName(String name){
            for(Item item : items){
                item.setName(name);
            }
            return this;
        }
        public ItemGroup setMaterial(Material material){
            for(Item item : items){
                item.setMaterial(material);
            }
            return this;
        }
        public ItemGroup setQuantity(int quantity){
            for(Item item : items){
                item.setQuantity(quantity);
            }
            return this;
        }
        public ItemGroup setGlow(boolean glow){
            for(Item item: items){
                item.setGlow(glow);
            }
            return this;
        }
        public ItemGroup setLore(List<String> lore){
            for(Item item : items){
                item.setLore(lore);
            }
            return this;
        }
        public ItemGroup removeLoreLine(int lineId){
            for(Item item: items){
                item.removeLoreLine(lineId);
            }
            return this;
        }
        public ItemGroup setClickHandler(GUIRunnable clickHandler){
            for(Item item : items){
                item.setClickHandler(clickHandler);
            }
            return this;
        }
        public ItemStack toBukkitItem() {
            if(items.isEmpty()) return null;
            return items.get(0).toBukkitItem();
        }
        public ItemGroup placeholder(String placeholder, String value){
            for(Item item : items){
                item.placeholder(placeholder, value);
            }
            return this;
        }
    }
    @SuppressWarnings("UnusedReturnValue")
    public static class Item {
        public String name="", id;
        public int quantity=1;
        public int slot=0;
        public boolean glow=false;
        public Map<Enchantment, Integer> enchantments=null;
        public int customModelData=-1;
        public Material material=Material.AIR;
        public List<String> lore=new ArrayList<>();
        public String skullId=null;
        public ItemStack stack=null;
        public GUIRunnable clickHandler=null;
        public Item(String id) {this.id = id;}
        public Item setMaterial(Material material) {
            this.material = material;
            return this;
        }
        public Item setItemStack(ItemStack stack) {
            material = stack.getType();
            quantity = stack.getAmount();
            ItemMeta meta = stack.getItemMeta();
            if(meta==null) return this;
            name=meta.getDisplayName();
            lore=meta.getLore();
            if(meta.getEnchants().size()==1&&meta.getEnchants().getOrDefault(Enchantment.DURABILITY, 0)==1) glow=true;
            else if(meta.hasEnchants()){
                enchantments = new HashMap<>(meta.getEnchants());
            }
            if(meta.hasCustomModelData()) customModelData = meta.getCustomModelData();
            return this;
        }
        public void setAsVanillaItemStack(ItemStack stack){
            this.stack = stack;
        }
        public Item setName(String name) {
            this.name = name;
            return this;
        }
        public Item setQuantity(int quantity) {
            this.quantity = quantity;
            return this;
        }
        public Item setLore(List<String> lore) {
            this.lore = lore;
            return this;
        }
        public Item setSlot(int slot) {
            this.slot = slot;
            return this;
        }
        public Item removeLoreLine(int lineId){
            if(lore.size()>lineId){
                lore.remove(lineId);
            }
            return this;
        }
        public Item setGlow(boolean glow){
            this.glow = glow;
            return this;
        }
        public Item setCustomModelData(int data){
            customModelData = data;
            return this;
        }
        public Item setSkullID(String skullID){
            this.skullId = skullID;
            return this;
        }
        public Item setClickHandler(GUIRunnable clickHandler) {
            this.clickHandler = clickHandler;
            return this;
        }
        public Item placeholder(String placeholder, String value){
            name = name.replace("%"+placeholder+"%", value);
            if(lore != null){
                lore.replaceAll(s -> s.replace("%"+placeholder+"%", value));
            }
            return this;
        }
        public ItemStack toBukkitItem() {
            if(stack!=null) return stack.clone();
            ItemStack item = new ItemStack(material, quantity);
            ItemMeta meta = item.getItemMeta();
            if(meta!= null){
                meta.setDisplayName(name);
                meta.setLore(lore);
                if(glow){
                    meta.addEnchant(Enchantment.DURABILITY, 1, true);
                    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                }else if(enchantments!=null){
                    for(Enchantment i: enchantments.keySet()){
                        meta.addEnchant(i, enchantments.get(i), true);
                    }
                }
                if(customModelData != -1) meta.setCustomModelData(customModelData);
                if(meta instanceof SkullMeta mt){
                    if(skullId!=null) {
                        try {
                            PlayerProfile profile = Bukkit.getServer().createPlayerProfile(UUID.randomUUID());
                            PlayerTextures textures = profile.getTextures();
                            textures.setSkin(new URL("https://textures.minecraft.net/texture/" + skullId));
                            profile.setTextures(textures);
                            mt.setOwnerProfile(profile);
                        } catch (MalformedURLException ignored) {
                            throw new RuntimeException(ignored);
                        }
                    }
                }
                item.setItemMeta(meta);
            }
            return item;
        }
        public Item copy() {
            Item item = new Item(id);
            item.name = name;
            item.quantity = quantity;
            item.material = material;
            item.lore = new ArrayList<>(lore);
            item.slot = slot;
            item.customModelData = customModelData;
            item.skullId = skullId;
            item.glow = glow;
            if(enchantments!=null) item.enchantments = new HashMap<>(enchantments);
            return item;
        }
    }
    public Map<Integer, Item> container=new HashMap<>();
    public int size;
    public Set<Integer> inputs=new HashSet<>();
    public String title;
    public Inventory lstInventory;
    public Item getItem(int slot){
        return container.get(slot);
    }
    public ItemGroup getItems(String id){
        List<Item> items = new ArrayList<>();
        for(Item item : container.values()){
            if(item.id.equals(id)){
                items.add(item);
            }
        }
        return new ItemGroup(items);
    }
    public GUI placeholder(String placeholder, String value){
        for(Item item : container.values()){
            item.placeholder(placeholder, value);
        }
        title = title.replace("%"+placeholder+"%", value);
        return this;
    }
    public Item addItem(String id, int slot){
        Item item = new Item(id);
        item.setSlot(slot);
        container.put(slot, item);
        return item;
    }
    public Item addItem(String id, String name, int slot, Material material, int quantity){
        Item item = new Item(id);
        item.setName(name);
        item.setMaterial(material);
        item.setQuantity(quantity);
        item.setSlot(slot);
        container.put(slot, item);
        return item;
    }
    public Item addItem(String id, int slot, ItemStack itemStack){
        Item item = new Item(id);
        item.setItemStack(itemStack);
        item.setSlot(slot);
        container.put(slot, item);
        return item;
    }
    public List<Item> addItem(String id, String name, List<Integer> slots, Material material, int quantity){
        List<Item> items = new ArrayList<>();
        for(int i : slots) {
            Item item = new Item(id);
            item.setName(name);
            item.setMaterial(material);
            item.setQuantity(quantity);
            item.setSlot(i);
            container.put(i, item);
        }
        return items;
    }
    public void removeItem(int slot){
        container.remove(slot);
    }
    protected ItemStack putNbt(Item item){
        ItemStack itemStack = item.toBukkitItem();
        NbtHook.setTag(itemStack, "bn.guiItem", item.id);
        return itemStack;
    }
    public void handleClick(int slot, GUISession session, InventoryAction action, InventoryClickEvent event){
        Item item = container.get(slot);
        if(item!= null && item.clickHandler != null){
            item.clickHandler.run(session, action, event);
        }
    }
    public void display(SimplePlayer player){
        Inventory inv = Bukkit.createInventory(null, size, new StringMessage(title).toString());
        lstInventory = inv;
        for(Item item : container.values()){
            inv.setItem(item.slot, putNbt(item));
        }
//        Item filleritem = new Item("_filler").setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ");
//        for(int i=0;i<inv.getSize();i++){
//            if(inv.getItem(i)==null||inv.getItem(i).getType()==Material.AIR){
//                inv.setItem(i, putNbt(filleritem));
//            }
//        }
        player.getPlayer().openInventory(inv);
    }
    public void update(){
        if(lstInventory != null){
            lstInventory.clear();
            for(Item item : container.values()){
                lstInventory.setItem(item.slot, putNbt(item));
            }
//            Item filleritem = new Item("_filler").setMaterial(Material.LIGHT_GRAY_STAINED_GLASS_PANE).setName(" ");
//            for(int i=0;i<lstInventory.getSize();i++){
//                if(lstInventory.getItem(i)==null||lstInventory.getItem(i).getType()==Material.AIR){
//                    lstInventory.setItem(i, putNbt(filleritem));
//                }
//            }
        }
    }
    public GUI copy(){
        GUI gui = new GUI();
        gui.size = size;
        gui.title = title;
        gui.inputs = inputs;
        for(int key : container.keySet()){
            gui.container.put(key, container.get(key).copy());
        }
        return gui;
    }
}
