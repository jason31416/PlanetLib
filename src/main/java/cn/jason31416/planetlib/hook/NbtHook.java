package cn.jason31416.planetlib.hook;

import de.tr7zw.changeme.nbtapi.NBT;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class NbtHook {
    public static boolean hasTag(ItemStack item, String tag) {
        if(item == null || item.getType().isAir()||item.getAmount()==0) return false;
        return NBT.get(item, nbt->(boolean)nbt.hasTag(tag));
    }
    public static void setTag(ItemStack item, String tag, String value) {
        if(item == null||item.getType().isAir()||item.getAmount()==0) return;
        NBT.modify(item, nbt->{
            nbt.setString(tag, value);
        });
    }
    public static void setTag(ItemStack item, String tag, Integer value) {
        if(item == null||item.getType().isAir()||item.getAmount()==0) return;
        NBT.modify(item, nbt->{
            nbt.setInteger(tag, value);
        });
    }
    public static String getTag(ItemStack item, String tag) {
        if(item == null||item.getType().isAir()||item.getAmount()==0) return null;
        return NBT.get(item, nbt->(String)nbt.getString(tag));
    }
    public static void addTag(ItemStack item, String tag) {
        if(item == null||item.getType().isAir()||item.getAmount()==0) return;
        NBT.modify(item, nbt->{
            nbt.setBoolean(tag, true);
        });
    }
    public static boolean hasTag(Entity entity, String tag){
        return NBT.get(entity, nbt->(boolean)nbt.hasTag(tag));
    }
    public static void addTag(Entity entity, String tag) {
        NBT.modify(entity, nbt->{
            nbt.setBoolean(tag, true);
        });
    }
    public static void setTag(Entity entity, String tag, String value){
        NBT.modify(entity, nbt->{
            nbt.setString(tag, value);
        });
    }
    public static String getTag(Entity entity, String tag){
        return NBT.get(entity, nbt->(String)nbt.getString(tag));
    }
}
