package cn.jason31416.planetlib.hook;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.map.MapMarker;
import cn.jason31416.planetlib.map.MapMarkerSet;
import cn.jason31416.planetlib.wrapper.SimpleWorld;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;

public class BlueMapHook {
    public static boolean enabled = false;
    public static Map<MapMarkerSet, Map<SimpleWorld, MarkerSet> > markers=new HashMap<>();
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("BlueMap") == null) {
            PlanetLib.instance.getLogger().info("\033[31mFailed to hook BlueMap\033[0m");
            enabled = false;
            return;
        }
        PlanetLib.instance.getLogger().info("\033[32mFound BlueMap, attempting to hook...\033[0m");
        BlueMapAPI.onEnable(api -> {
            markers.clear();
            enabled = true;
            PlanetLib.instance.getLogger().info("\033[32mHooked into Bluemap!\033[0m");
        });
    }
}
