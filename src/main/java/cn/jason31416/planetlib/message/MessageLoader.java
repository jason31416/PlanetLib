package cn.jason31416.planetlib.message;

import cn.jason31416.planetlib.util.MapTree;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class MessageLoader {
    public MapTree messageConfig;
    public MessageLoader(MapTree messageConfig) {
        this.messageConfig = messageConfig;
    }
    public MessageLoader(File filePath) {
        try{
            this.messageConfig = MapTree.fromYaml(Files.readString(filePath.toPath()));
        }catch (Exception ignored){
            throw new RuntimeException("Failed to load message config file!");
        }
    }
    public StringMessage getMessage(String key, String defaultMessage) {
        if(messageConfig.isList(key)){
            return new StringMessage(String.join("\n", messageConfig.getStringList(key)));
        }
        if(messageConfig.contains(key)) return new StringMessage(messageConfig.getString(key));
        return defaultMessage == null? null : new StringMessage(defaultMessage);
    }
    public String getRawMessage(String key, String defaultMessage) {
        if(messageConfig.contains(key)) return messageConfig.getString(key);
        return defaultMessage;
    }
    public MessageList getList(String key, List<String> defaultList) {
        if(messageConfig.isList(key)) return new MessageList(messageConfig.getStringList(key));
        return new MessageList(defaultList);
    }
}
