package cn.jason31416.planetlib;

import cn.jason31416.planetlib.gui.GUIEventHandler;
import cn.jason31416.planetlib.hook.BlueMapHook;
import cn.jason31416.planetlib.hook.VaultHook;
import cn.jason31416.planetlib.message.StringMessage;
import cn.jason31416.planetlib.util.Config;
import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import de.tr7zw.changeme.nbtapi.NBT;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class PlanetLib extends JavaPlugin {
    public static JavaPlugin instance;
    public static FoliaLib foliaLib;
    @Override
    public void onEnable() {
        initialize(this);
    }
    public static PlatformScheduler getScheduler(){
        return foliaLib.getScheduler();
    }
    public static void initialize(JavaPlugin plugin, Required... require) {
        instance=plugin;
        StringMessage.bukkitAudiences = BukkitAudiences.create(plugin);
        StringMessage.miniMessage = MiniMessage.miniMessage();

        Set<Required> requirements = Arrays.stream(require).collect(Collectors.toSet());

        if(requirements.contains(Required.VAULT)) VaultHook.init();
        if(requirements.contains(Required.BLUEMAP)) BlueMapHook.init();
        if(requirements.contains(Required.NBT)) NBT.preloadApi();

        plugin.saveDefaultConfig();
        Config.start(plugin);

        foliaLib = new FoliaLib(plugin);
        instance.getServer().getPluginManager().registerEvents(new GUIEventHandler(), plugin);
    }
    @Override
    public void onDisable(){
        disable();
    }

    public static void info(String message){
        PluginLogger.info(message);
    }
    public static void warning(String message){
        PluginLogger.warning(message);
    }
    public static void error(String message){
        PluginLogger.error(message);
    }

    public static void disable(){
        StringMessage.bukkitAudiences.close();
    }
}
