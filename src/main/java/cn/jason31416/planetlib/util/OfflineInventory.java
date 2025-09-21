package cn.jason31416.planetlib.util;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.NbtHook;
import de.tr7zw.changeme.nbtapi.NBTCompoundList;
import de.tr7zw.changeme.nbtapi.NBTContainer;
import de.tr7zw.changeme.nbtapi.NBTFile;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("DataFlowIssue")
@Deprecated
public class OfflineInventory implements Inventory, AutoCloseable {
    private final NBTFile nbt;
    private final Map<Integer, ItemStack> itemMap = new ConcurrentHashMap<>();
    private final NbtCaseRecorder caseRecorder = new NbtCaseRecorder();

    public OfflineInventory(UUID uuid) throws FileNotFoundException {
        File folder = PlanetLib.instance.getServer().getWorlds().get(0).getWorldFolder();
        File file = new File(folder, "playerdata/" + uuid + ".dat");
        new File(folder, "playerdata/" + uuid + ".dat_old").delete();
        if (!file.exists())
            throw new FileNotFoundException("Player " + uuid + "'s data not found");
        try {
            nbt = new NBTFile(file);
            NBTCompoundList inv = Util.getFirstNonnullOne(nbt.getCompoundList("inventory"), nbt.getCompoundList("Inventory"));
            for(ReadWriteNBT obj : inv) {
                caseRecorder.recordCase(Pair.of("count", obj.getInteger("count")), Pair.of("Count", obj.getInteger("Count")));
                caseRecorder.recordCase(Pair.of("id", obj.getString("id")), Pair.of("Id", obj.getString("Id")));
                ItemStack item = new ItemStack(
                        Util.getFirstNonnullOne(Material.getMaterial(NamespacedKey.fromString(obj.getString("id")).getKey()), Material.getMaterial(NamespacedKey.fromString(obj.getString("Id")).getKey())),
                        Util.getFirstNonnullOne(obj.getInteger("count"), obj.getInteger("Count"))
                );
                int slot = Util.getFirstNonnullOne(obj.getInteger("slot"), obj.getInteger("Slot"));
                caseRecorder.recordCase(Pair.of("slot", obj.getInteger("slot")), Pair.of("Slot", obj.getInteger("Slot")));
                ReadWriteNBT tag = Util.getFirstNonnullOne(obj.getCompound("tag"), obj.getCompound("Tag"));
                caseRecorder.recordCase(Pair.of("tag", obj.getCompound("tag")), Pair.of("Tag", obj.getCompound("Tag")));
                if(tag != null) {
                    for(String key : tag.getKeys()) {
                        Integer intValue = tag.getInteger(key);
                        String stringValue = tag.getString(key);
                        if(intValue != null)
                            NbtHook.setTag(item, key, intValue);
                        else
                            NbtHook.setTag(item, key, stringValue);
                    }
                }
                itemMap.put(slot, item);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        NBTCompoundList inv = Util.getFirstNonnullOne(nbt.getCompoundList("inventory"), nbt.getCompoundList("Inventory"));
        inv.clear();
        for(int slot : itemMap.keySet()) {
            ItemStack item = itemMap.get(slot);
            ReadWriteNBT itemTag = new NBTContainer();
            Set<String> tags = NbtHook.getTags(item);
            if(tags != null) {
                ReadWriteNBT tagTag = itemTag.getOrCreateCompound(caseRecorder.fixCase("tag"));
                for(String tag : tags) {
                    ReadWriteNBT subTag = tagTag.getOrCreateCompound(tag);
                    Integer intValue = Util.tryParseInt(NbtHook.getTag(item, tag));
                    String stringValue = NbtHook.getTag(item, tag);
                    if(intValue != null)
                        subTag.setInteger(tag, intValue);
                    else
                        subTag.setString(tag, stringValue);
                }
            }
            itemTag.setString(caseRecorder.fixCase("id"), item.getType().getKey().toString());
            itemTag.setInteger(caseRecorder.fixCase("slot"), slot);
            itemTag.setInteger(caseRecorder.fixCase("count"), item.getAmount());
            inv.add(0, itemTag);
        }
        nbt.save();
    }

    @Override
    public int getSize() {
        return 41;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    @Deprecated
    public void setMaxStackSize(int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @Nullable ItemStack getItem(int i) {
        return itemMap.get(i);
    }

    @Override
    public void setItem(int i, @Nullable ItemStack item) {
        itemMap.put(i, item);
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> addItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> ret = new HashMap<>();
        outer:
        for(ItemStack item : items) {
            for(int i = 0; i < 36; i++) {
                if(!itemMap.containsKey(i)) {
                    itemMap.put(i, item);
                    ret.put(i, item);
                    continue outer;
                } else if(itemMap.get(i).getType() == item.getType()) {
                    if(itemMap.get(i).getAmount() + item.getAmount() <= 64) {
                        itemMap.get(i).setAmount(itemMap.get(i).getAmount() + item.getAmount());
                        ret.put(i, item);
                        continue outer;
                    } else {
                        itemMap.get(i).setAmount(64);
                        item.setAmount(64 - item.getAmount());
                        ret.put(i, item.clone());
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public @NotNull HashMap<Integer, ItemStack> removeItem(@NotNull ItemStack... items) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> ret = new HashMap<>();
        new HashMap<>(itemMap).forEach((slot, item) -> {
            for(ItemStack item1 : items) {
                if(item1.equals(item)) {
                    ret.put(slot, item);
                    itemMap.remove(slot);
                    break;
                }
            }
        });
        return ret;
    }

    @Override
    public @NotNull ItemStack[] getContents() {
        return itemMap.values().toArray(new ItemStack[0]);
    }

    @Override
    public void setContents(@NotNull ItemStack[] items) throws IllegalArgumentException {
        itemMap.clear();
        for(int i = 0; i < 36; i++) {
            if(i < items.length) {
                itemMap.put(i, items[i]);
            } else break;
        }
    }

    @Override
    @Deprecated
    public @NotNull ItemStack[] getStorageContents() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public void setStorageContents(@NotNull ItemStack[] itemStacks) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(@NotNull Material material) throws IllegalArgumentException {
        return itemMap.values().stream().anyMatch(item -> item.getType() == material);
    }

    @Override
    public boolean contains(@Nullable ItemStack item) {
        return itemMap.values().stream().allMatch(item1 -> item1.equals(item));
    }

    @Override
    public boolean contains(@NotNull Material material, int i) throws IllegalArgumentException {
        return itemMap.containsKey(i) && itemMap.get(i).getType() == material;
    }

    @Override
    public boolean contains(@Nullable ItemStack item, int i) {
        return itemMap.containsKey(i) && itemMap.get(i).equals(item);
    }

    @Override
    public boolean containsAtLeast(@Nullable ItemStack item, int i) {
        return itemMap.containsKey(i) && itemMap.get(i).getType() == item.getType() && itemMap.get(i).getAmount() >= item.getAmount();
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@NotNull Material material) throws IllegalArgumentException {
        HashMap<Integer, ItemStack> ret = new HashMap<>();
        itemMap.forEach((slot, item) -> {
            if(item.getType() == material)
                ret.put(slot, item);
        });
        return ret;
    }

    @Override
    public @NotNull HashMap<Integer, ? extends ItemStack> all(@Nullable ItemStack item) {
        HashMap<Integer, ItemStack> ret = new HashMap<>();
        itemMap.forEach((slot, item1) -> {
            if(item1.equals(item))
                ret.put(slot, item);
        });
        return ret;
    }

    @Override
    public int first(@NotNull Material material) throws IllegalArgumentException {
        final int[] slot = {-1};
        itemMap.forEach((slot1, item) -> {
            if(item.getType() == material && slot[0] != -1)
                slot[0] = slot1;
        });
        return slot[0];
    }

    @Override
    public int first(@NotNull ItemStack item) {
        final int[] slot = {-1};
        itemMap.forEach((slot1, item1) -> {
            if(item1.equals(item) && slot[0] != -1)
                slot[0] = slot1;
        });
        return slot[0];
    }

    @Override
    public int firstEmpty() {
        for(int i = 0; i < 36; i++) {
            if(!itemMap.containsKey(i) || itemMap.get(i).getType() == Material.AIR) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    @Override
    public void remove(@NotNull Material material) throws IllegalArgumentException {
        new HashMap<>(itemMap).forEach((slot, item) -> {
            if(item.getType() == material)
                itemMap.remove(slot);
        });
    }

    @Override
    public void remove(@NotNull ItemStack item) {
        new HashMap<>(itemMap).forEach((slot, item1) -> {
            if(item1.equals(item))
                itemMap.remove(slot);
        });
    }

    @Override
    public void clear(int i) {
        itemMap.remove(i);
    }

    @Override
    public void clear() {
        itemMap.clear();
    }

    @Override
    @Deprecated
    public @NotNull List<HumanEntity> getViewers() {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public @NotNull InventoryType getType() {
        return InventoryType.PLAYER;
    }

    @Override
    @Deprecated
    public @Nullable InventoryHolder getHolder() {
        return null;
    }

    @Override
    public @NotNull ListIterator<ItemStack> iterator() {
        return itemMap.values().stream().toList().listIterator();
    }

    @Override
    @Deprecated
    public @NotNull ListIterator<ItemStack> iterator(int i) {
        return List.of(itemMap.get(i)).listIterator();
    }

    @Override
    @Deprecated
    public @Nullable Location getLocation() {
        return null;
    }
}
