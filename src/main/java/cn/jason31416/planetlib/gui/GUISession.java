package cn.jason31416.planetlib.gui;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

public class GUISession {
    @Getter
    private final static Map<SimplePlayer, GUISession> sessions = new HashMap<>();
    @Getter
    public final SimplePlayer player;

    @Getter
    private boolean closed = false;

    @Getter
    private GUI currentGUI;

    private boolean isSwitching = false;

    public GUISession(SimplePlayer player) {
        this.player = player;
    }

    /**
     * Called when the GUI is first displayed.
     * Should be overridden by subclasses to set up the GUI if needed.
     *
     * @param gui
     */
    public void setup(GUI gui){}

    public void onClose(){}

    @ApiStatus.Internal
    public void close(){
        if(isSwitching) return;
        onClose();
        sessions.remove(player);
        closed = true;
    }
    public void display(GUI gui){
//        if(currentGUI != null && currentGUI.getInventory().equals(player.getPlayer().getOpenInventory().getTopInventory())){
//            currentGUI._closeGUI();
//        }
        isSwitching = true;
        sessions.put(player, this);
        setup(gui);
        gui.setSession(this);
        gui.update();
        gui.display();
        currentGUI = gui;
        isSwitching = false;
    }
}
