package cn.jason31416.planetlib.wrapper;

import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

public final class SimpleWorld implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private transient World world;
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

    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(world.getUID());
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        UUID worldUid = (UUID) in.readObject();
        world = Bukkit.getWorld(worldUid);
    }
}
