package cn.jason31416.planetlib.util;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.MessageList;
import cn.jason31416.planetlib.message.MessageLoader;
import cn.jason31416.planetlib.message.StringMessage;
import lombok.SneakyThrows;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Lang {
    public static MessageLoader messageLoader, defaultLoader=null;
    public static String fileName;

    @SneakyThrows
    public static void init(String fileName){
        Util.savePluginResource(fileName);

        Lang.fileName = fileName;
        messageLoader = new MessageLoader(new File(PlanetLib.instance.getDataFolder(), fileName));

        try (InputStream jarStream = PlanetLib.getInstance().getResource(fileName)) {
            if (jarStream == null) {
                PluginLogger.warning("[PlanetLib] Default language file not found in jar resource; default language keys skipped.");
                return;
            }
            String jarYaml = new String(jarStream.readAllBytes(), StandardCharsets.UTF_8);
            defaultLoader = new MessageLoader(MapTree.fromYaml(jarYaml));
        }
    }

    public static Message getMessage(String key){
        StringMessage ret = messageLoader.getMessage(key, defaultLoader!=null?"":"&c"+key);
        if(ret.toString().isEmpty()){
            return defaultLoader.getMessage(key, "&c"+key);
        }
        return ret;
    }

    public static Message getMessage(String key, String defaultMessage){
        return messageLoader.getMessage(key, defaultMessage);
    }

    public static MessageList getMessageList(String key){
        return messageLoader.getList(key, new ArrayList<>());
    }
}
