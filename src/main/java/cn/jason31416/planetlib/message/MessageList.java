package cn.jason31416.planetlib.message;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MessageList implements Message {
    @Getter
    private final List<String> content;
    public MessageList(List<String> content) {
        this.content = content;
    }
    public MessageList add(String placeholder, Object value){
        content.replaceAll(s -> s.replace("%"+placeholder+"%", value.toString()));
        return this;
    }

    @Override
    public MessageList copy() {
        return new MessageList(new ArrayList<>(content));
    }

    @Override
    public void send(CommandSender sender) {
        for (String s : content) {
            new StringMessage(s).send(sender);
        }
    }

    @Override
    public void sendActionbar(Player sender) {
        throw new UnsupportedOperationException("Cannot send list as actionbar!");
    }

    @Override
    public Component toComponent() {
        return new StringMessage(String.join("\n", content)).toComponent();
    }

    public List<String> asList() {
        ArrayList<String> list = new ArrayList<>(content);
        list.replaceAll(s -> new StringMessage(s).toString());
        return list;
    }
}
