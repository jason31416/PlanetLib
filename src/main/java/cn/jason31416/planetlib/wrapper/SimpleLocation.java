package cn.jason31416.planetlib.wrapper;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SimpleLocation(double x, double y, double z, SimpleWorld world) implements ConfigurationSerializable {
    public SimpleLocation getRelative(double dx, double dy, double dz) {
        return new SimpleLocation(x + dx, y + dy, z + dz, world);
    }

    public SimpleLocation getBlockLocation() {
        return new SimpleLocation(Math.floor(x), Math.floor(y), Math.floor(z), world);
    }

    public Block getBlock() {
        return world.getBukkitWorld().getBlockAt((int) x, (int) y, (int) z);
    }

    public Location getBukkitLocation() {
        return new Location(world.getBukkitWorld(), x, y, z);
    }

    public SimpleChunkLocation getChunkLocation() {
        return new SimpleChunkLocation((int) Math.floor(x / 16), (int) Math.floor(z / 16), world);
    }

    public static SimpleLocation of(Location location) {
        return new SimpleLocation(location.getX(), location.getY(), location.getZ(), SimpleWorld.of(location.getWorld()));
    }

    public static SimpleLocation of(Block block) {
        return new SimpleLocation(block.getX(), block.getY(), block.getZ(), SimpleWorld.of(block.getWorld()));
    }

    public static SimpleLocation of(double x, double y, double z, SimpleWorld world) {
        return new SimpleLocation(x, y, z, world);
    }

    public Material getBlockMaterial() {
        return getBlock().getType();
    }
    public void setBlockMaterial(Material material) {
        getBlock().setType(material);
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleLocation other)) {
            return false;
        }
        return x == other.x && y == other.y && z == other.z && world.equals(other.world);
    }

    public int hashCode() {
        return Double.hashCode(x) ^ Double.hashCode(y) ^ Double.hashCode(z) ^ world.hashCode();
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", this.world().getName());
        data.put("x", this.x);
        data.put("y", this.y);
        data.put("z", this.z);
        return data;
    }

    public static SimpleLocation deserialize(Map<String, Object> map) {
        SimpleWorld world = SimpleWorld.of(UUID.fromString((String) map.get("world")));
        int x = (int) map.get("x");
        int y = (int) map.get("y");
        int z = (int) map.get("z");
        return new SimpleLocation(x, y, z, world);
    }
}
