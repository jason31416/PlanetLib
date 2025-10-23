package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class GUISession {
    @Getter
    private static Map<SimplePlayer, GUISession> sessions = new HashMap<>();

    public SimplePlayer player;
}
