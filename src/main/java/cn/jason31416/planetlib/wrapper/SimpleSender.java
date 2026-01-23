package cn.jason31416.planetlib.wrapper;

import cn.jason31416.planetlib.message.Message;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SimpleSender{
    @Getter
    private final CommandSender sender;

    public SimpleSender(CommandSender sender) {
        this.sender = sender;
    }

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

    public CommandSender sender() {
        return sender;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SimpleSender) obj;
        return Objects.equals(this.sender, that.sender);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sender);
    }

    @Override
    public String toString() {
        return "SimpleSender[" +
                "sender=" + sender + ']';
    }

}
