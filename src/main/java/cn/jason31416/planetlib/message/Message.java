package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public interface Message extends ComponentLike {
    static void useTheme(MessageTheme theme) {
        MessageThemeHolder.currentTheme = theme;
    }
    static void useTheme(String themeID) {
        useTheme(MessageTheme.getThemes().get(themeID));
    }

    static String applyTheme(String text) {
        return MessageThemeHolder.currentTheme != null ? MessageThemeHolder.currentTheme.applyTheme(text) : text;
    }

    Message add(String key, Object value);
    String toString();
    default String toFormatted(){
        return toString();
    }
    Message copy();
    void send(CommandSender sender);
    default void send(SimpleSender sender){
        send(sender.sender());
    }
    default void send(SimplePlayer player){
        if(player.isOnline()) send(player.getPlayer());
    }
    void sendActionbar(Player sender);
    default void sendActionbar(SimplePlayer player){
        if(player.getPlayer() != null) sendActionbar(player.getPlayer());
    }
    default @NotNull Component asComponent(){
        return toComponent();
    }
    Component toComponent();
    default void broadcast(){
        for(Player player : Bukkit.getOnlinePlayers()){
            send(player);
        }
        send(Bukkit.getConsoleSender());
    }
    default void send(Collection<SimplePlayer> players){
        for(SimplePlayer player : players){
            if(player.isOnline()) send(player.getPlayer());
        }
    }
    public static Message of(Component component){
        return new StringMessage(MiniMessage.miniMessage().serialize(component));
    }
    public static Message of(String content){
        return new StringMessage(content);
    }
    public static MessageList of(List<String> content){
        return new MessageList(content);
    }
}

class MessageThemeHolder {
    static MessageTheme currentTheme = null;
}
