package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StringMessage implements Message {
    public static BukkitAudiences bukkitAudiences;
    public static MiniMessage miniMessage;
    String content;
    public StringMessage(String content) {
        this.content = content
                .replace("§", "&")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&l", "<bold>")
                .replace("&r", "<reset>")
                .replace("&o", "<italic>");
    }
    public StringMessage add(String placeholder, Object value){
        content = content.replace("%"+placeholder+"%", (value instanceof String)?(String)value:value.toString());
        return this;
    }
    public String toString(){
        return LegacyComponentSerializer.legacySection().serialize(MiniMessage.miniMessage().deserialize(InternalPlaceholder.replacePlaceholders(content, null)));
    }
    public String toFormatted(){
        return MiniMessage.miniMessage().serialize(MiniMessage.miniMessage().deserialize(InternalPlaceholder.replacePlaceholders(content, null)));
    }
    public void send(CommandSender player){
        Component component = MiniMessage.miniMessage().deserialize(InternalPlaceholder.replacePlaceholders(content, null));
        if(player instanceof Player pl) pl.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
        else player.sendMessage(LegacyComponentSerializer.legacySection().serialize(component));
    }
    public void sendActionbar(Player player){
        bukkitAudiences.player(player).sendActionBar(MiniMessage.miniMessage().deserialize(InternalPlaceholder.replacePlaceholders(content, SimplePlayer.of(player))));
    }
    public boolean equals(Object obj){
        if(obj instanceof StringMessage){
            return ((StringMessage)obj).content.equals(content);
        }
        return false;
    }
}
