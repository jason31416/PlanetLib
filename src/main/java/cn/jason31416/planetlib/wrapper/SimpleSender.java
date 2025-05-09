package cn.jason31416.planetlib.wrapper;

import cn.jason31416.planetlib.message.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public record SimpleSender(CommandSender sender) {
    public static SimpleSender of(@NotNull CommandSender sender) {
        return new SimpleSender(sender);
    }
    public boolean isPlayer() {
        return sender instanceof Player;
    }
    public SimplePlayer toPlayer() {
        if (isPlayer()) {
            return SimplePlayer.of((Player) sender);
        }
        throw new IllegalStateException("CommandSender is not a player");
    }
    public void sendMessage(Message message) {
        message.send(sender);
    }
}
