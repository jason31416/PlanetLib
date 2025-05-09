package cn.jason31416.planetlib.wrapper;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record SimpleChunkLocation(int x, int z, SimpleWorld world) implements ConfigurationSerializable {
    public enum Direction {
        EAST,
        WEST,
        NORTH,
        SOUTH
    }
    public World getBukkitWorld() {
        return world.getBukkitWorld();
    }

    public Chunk getChunk() {
        return world.getBukkitWorld().getChunkAt(x, z);
    }

    public SimpleChunkLocation getRelative(int dx, int dz) {
        return new SimpleChunkLocation(x + dx, z + dz, world);
    }

    public SimpleChunkLocation getRelative(Direction dir){
        if(dir==Direction.EAST){
            return getRelative(1, 0);
        }else if(dir==Direction.WEST){
            return getRelative(-1, 0);
        }else if(dir==Direction.SOUTH){
            return getRelative(0, 1);
        }else{
            return getRelative(0, -1);
        }
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleChunkLocation other)) {
            return false;
        }
        return this.x == other.x && this.z == other.z && this.world.equals(other.world);
    }

    public String toString() {
        return "SimpleChunkLocation(x=" + x + ", z=" + z + ", world=" + world + ")";
    }

    public static SimpleChunkLocation of(Chunk chunk) {
        return new SimpleChunkLocation(chunk.getX(), chunk.getZ(), SimpleWorld.of(chunk.getWorld()));
    }
    public double distance(SimpleChunkLocation other) {
        double dx = this.x - other.x;
        double dz = this.z - other.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
    public Collection<SimpleChunkLocation> getAdjacentChunks() {
        int[] dx = {-1, 0, 1, 0}, dz = {0, -1, 0, 1};
        Collection<SimpleChunkLocation> result = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            result.add(this.getRelative(dx[i], dz[i]));
        }
        return result;
    }
    public Collection<SimpleChunkLocation> getDiagAdjacentChunks() {
        int[] dx = {-1, -1, 1, 1}, dz = {1, -1, 1, -1};
        Collection<SimpleChunkLocation> result = new java.util.ArrayList<>();
        for (int i = 0; i < 4; i++) {
            result.add(this.getRelative(dx[i], dz[i]));
        }
        return result;
    }
    public Collection<SimpleChunkLocation> getEightAdjacentChunks() {
        int[] dx = {-1, 0, 1, 0, -1, -1, 1, 1}, dz = {0, -1, 0, 1, -1, 1, -1, 1};
        Collection<SimpleChunkLocation> result = new java.util.ArrayList<>();
        for (int i = 0; i < 8; i++) {
            result.add(this.getRelative(dx[i], dz[i]));
        }
        return result;
    }

    public static SimpleChunkLocation of(int x, int z, SimpleWorld world) {
        return new SimpleChunkLocation(x, z, world);
    }

    public static SimpleChunkLocation of(int x, int z) {
        return new SimpleChunkLocation(x, z, SimpleWorld.defaultWorld());
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("world", this.world().getName());
        data.put("x", this.x);
        data.put("z", this.z);
        return data;
    }

    public static SimpleChunkLocation deserialize(Map<String, Object> map) {
        SimpleWorld world = SimpleWorld.of(UUID.fromString((String) map.get("world")));
        int x = (int) map.get("x");
        int z = (int) map.get("z");
        return new SimpleChunkLocation(x, z, world);
    }
}
