package cn.jason31416.planetlib.wrapper;

import cn.jason31416.planetlib.PlanetLib;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class SimpleLocation implements ConfigurationSerializable, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    @Getter
    private final double x;
    @Getter
    private final double y;
    @Getter
    private final double z;
    @Getter
    private final SimpleWorld world;

    public SimpleLocation(double x, double y, double z, SimpleWorld world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }

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

    public CompletableFuture<Block> getLoadedBlock() {
        CompletableFuture<Block> ret = new CompletableFuture<>();
        PlanetLib.getScheduler().runAtLocation(getBukkitLocation(), task -> {
            ret.complete(getBlock());
        });
        return ret;
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
        data.put("world", this.world().getBukkitWorld().getUID().toString());
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

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    public double z() {
        return z;
    }

    public SimpleWorld world() {
        return world;
    }

    @Override
    public String toString() {
        return "SimpleLocation[" +
                "x=" + x + ", " +
                "y=" + y + ", " +
                "z=" + z + ", " +
                "world=" + world + ']';
    }

}
