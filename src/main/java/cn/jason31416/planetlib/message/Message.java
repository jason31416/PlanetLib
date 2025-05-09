package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public interface Message {
    Message add(String key, Object value);
    String toString();
    default String toFormatted(){
        return toString();
    }
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
    public static Message of(String content){
        return new StringMessage(content);
    }
}
