package cn.jason31416.planetlib.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.UUID;

public class SimpleWorld {
    private final World world;
    public SimpleWorld(World world) {
        this.world = world;
    }
    public World getBukkitWorld() {
        return world;
    }
    public String getName() {
        return world.getName();
    }
    public static SimpleWorld of(World world){
        return new SimpleWorld(world);
    }
    public static SimpleWorld of(UUID worldUUID){
        return new SimpleWorld(Bukkit.getWorld(worldUUID));
    }
    public static SimpleWorld of(String worldName){
        return new SimpleWorld(Bukkit.getWorld(worldName));
    }
    public static SimpleWorld defaultWorld(){
        return new SimpleWorld(Bukkit.getWorlds().get(0));
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleWorld other)) {
            return false;
        }
        return this.world.equals(other.world);
    }
    public int hashCode() {
        return world.getUID().hashCode();
    }
    public String toString(){
        return "SimpleWorld("+ getName() + ")";
    }
}
