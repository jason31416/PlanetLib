package cn.jason31416.planetlib.util;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;
import cn.jason31416.planetlib.message.MessageLoader;

import java.io.File;
import java.util.ArrayList;

public class Lang {
    public static MessageLoader messageLoader;

    public static void init(String fileName){
        Util.savePluginResource(fileName);
        messageLoader = new MessageLoader(new File(PlanetLib.instance.getDataFolder(), fileName));
    }

    public Message getMessage(String key){
        return messageLoader.getMessage(key, "&c"+key);
    }

    public MessageList getMessageList(String key){
        return new MessageList(messageLoader.getList(key, new ArrayList<>()));
    }
}
