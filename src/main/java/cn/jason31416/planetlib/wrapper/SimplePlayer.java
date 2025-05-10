package cn.jason31416.planetlib.wrapper;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.hook.VaultHook;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public record SimplePlayer(OfflinePlayer offlinePlayer) implements ConfigurationSerializable {
    public String getName() {
        return offlinePlayer.getName();
    }

    public UUID getUUID() {
        return offlinePlayer.getUniqueId();
    }

    public boolean isOnline() {
        return offlinePlayer.isOnline();
    }

    public SimpleLocation getLocation() {
        if(!isOnline()) throw new IllegalStateException("Player is not online");
        return SimpleLocation.of(getPlayer().getLocation());
    }

    public Player getPlayer() {
        if(!isOnline()) throw new IllegalStateException("Player is not online");
        return offlinePlayer.getPlayer();
    }

    public void sendMessage(Message message) {
        if(!isOnline()) return;
        message.send(getPlayer());
    }
    public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut){
        if(!isOnline()) return;
        getPlayer().sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    public double getBalance(){
        return VaultHook.getBalance(offlinePlayer);
    }
    public void addBalance(double amount){
        VaultHook.depositBalance(offlinePlayer, amount);
    }
    public boolean withdrawBalance(double amount){
        return VaultHook.withdrawBalance(offlinePlayer, amount); // this is for multiplier support
    }
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimplePlayer other)) {
            return false;
        }
        return offlinePlayer.getUniqueId().equals(other.offlinePlayer.getUniqueId());
    }

    public CompletableFuture<Boolean> teleport(SimpleLocation location){
        return PlanetLib.getScheduler().teleportAsync(getPlayer(), location.getBukkitLocation());
    }

    public int hashCode() {
        return offlinePlayer.getUniqueId().hashCode();
    }

    public static SimplePlayer of(OfflinePlayer offlinePlayer) {
        return new SimplePlayer(offlinePlayer);
    }
    public static SimplePlayer of(Player player) {
        return new SimplePlayer(player);
    }
    public static SimplePlayer of(CommandSender player) {
        if(player instanceof Player) return new SimplePlayer((Player) player);
        return null;
    }
    public static SimplePlayer of(UUID uuid) {
        return new SimplePlayer(Bukkit.getOfflinePlayer(uuid));
    }
    @SuppressWarnings("deprecation")
    public static SimplePlayer of(String name) {
        return new SimplePlayer(Bukkit.getOfflinePlayer(name));
    }

    @NotNull
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("uuid", getUUID().toString());
        return map;
    }
    @NotNull
    public static SimplePlayer deserialize(@NotNull Map<String, Object> map) {
        UUID uuid = UUID.fromString((String) map.get("uuid"));
        return new SimplePlayer(Bukkit.getOfflinePlayer(uuid));
    }
}
