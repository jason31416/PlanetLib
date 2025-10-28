package cn.jason31416.planetlib.wrapper;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.PAPIHook;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;
import cn.jason31416.planetlib.message.StringMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class SimpleItemStack {
    public Message name=Message.of("");
    public int quantity=1;
    public boolean glow=false;
    public Map<Enchantment, Integer> enchantments=null;
    public int customModelData=-1;
    public Material material=Material.AIR;
    public MessageList lore=null;
    public String skullId=null;
    public ItemStack stack=null;
    public SimpleItemStack setMaterial(Material material) {
        this.material = material;
        return this;
    }
    public SimpleItemStack setItemStack(ItemStack stack) {
        material = stack.getType();
        quantity = stack.getAmount();
        ItemMeta meta = stack.getItemMeta();
        if(meta==null) return this;
        if(meta.hasDisplayName())
            name=Message.of(meta.displayName());
        if(meta.hasLore())
            lore=Message.of(meta.getLore());
        if(meta.getEnchants().size()==1&&meta.getEnchants().getOrDefault(Enchantment.UNBREAKING, 0)==1) glow=true;
        else if(meta.hasEnchants()){
            enchantments = new HashMap<>(meta.getEnchants());
        }
        if(meta.hasCustomModelData()) customModelData = meta.getCustomModelData();
        return this;
    }
    public void setAsVanillaItemStack(ItemStack stack){
        this.stack = stack;
    }
    public SimpleItemStack setName(String name) {
        this.name = Message.of(name);
        return this;
    }
    public SimpleItemStack setName(Message name) {
        this.name = name;
        return this;
    }
    public SimpleItemStack setQuantity(int quantity) {
        this.quantity = quantity;
        return this;
    }
    public SimpleItemStack setLore(List<String> lore) {
        this.lore = Message.of(lore);
        return this;
    }
    public SimpleItemStack setLore(MessageList lore) {
        this.lore = lore;
        return this;
    }
    public SimpleItemStack setGlow(boolean glow){
        this.glow = glow;
        return this;
    }
    public SimpleItemStack setCustomModelData(int data){
        customModelData = data;
        return this;
    }
    public SimpleItemStack setSkullID(String skullID){
        this.skullId = skullID;
        return this;
    }
    public SimpleItemStack placeholder(String placeholder, String value){
        name = name.add(placeholder, value);
        if(lore != null){
            lore.add(placeholder, value);
        }
        return this;
    }
    public SimpleItemStack papi(SimplePlayer player){
        if(!PlanetLib.isPAPIEnabled) return this;
        name = Message.of(PAPIHook.replace(player, name.toFormatted()));
        if(lore != null){
            lore.getContent().replaceAll(text -> PAPIHook.replace(player, text));
        }
        return this;
    }
    public ItemStack toBukkitItem() {
        if(stack!=null) return stack.clone();
        ItemStack item = new ItemStack(material, quantity);
        ItemMeta meta = item.getItemMeta();
        if(meta!= null){
            if(!name.toString().isEmpty())
                meta.setDisplayName(name.toString());
            if(lore != null)
                meta.setLore(lore.asList());
            if(glow){
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }else if(enchantments!=null){
                for(Enchantment i: enchantments.keySet()){
                    meta.addEnchant(i, enchantments.get(i), true);
                }
            }
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);
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
    public SimpleItemStack copy() {
        SimpleItemStack item = new SimpleItemStack();
        item.name = name.copy();
        item.quantity = quantity;
        item.material = material;
        if(lore!=null)
            item.lore = lore.copy();
        item.customModelData = customModelData;
        item.skullId = skullId;
        item.glow = glow;
        if(enchantments!=null) item.enchantments = new HashMap<>(enchantments);
        return item;
    }
}
