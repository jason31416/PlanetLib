package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public abstract class GUISession {
    @Getter
    private final static Map<SimplePlayer, GUISession> sessions = new HashMap<>();
    @Getter
    public final SimplePlayer player;

    @Getter
    private boolean closed = false;

    @Getter
    private GUI currentGUI;

    public GUISession(SimplePlayer player) {
        this.player = player;
    }
    public abstract void setup(GUI gui);

    @ApiStatus.Internal
    public void close(){
        sessions.remove(player);
        closed = true;
    }
    public void display(GUI gui){
        sessions.put(player, this);
        setup(gui);
        gui.update();
        gui.display(this);
        currentGUI = gui;
    }
}
