package cn.jason31416.planetlib.hook;

import cn.jason31416.planetlib.PlanetLib;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class VaultHook {
    private static RegisteredServiceProvider<Economy> rsp=null;
    public static void init() {
        if(Bukkit.getPluginManager().getPlugin("Vault") == null || Bukkit.getServer().getServicesManager().getRegistration(Economy.class) == null) {
            PlanetLib.instance.getLogger().info("\033[31mFailed to hook Vault!\033[0m");
            return;
        }
        rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        PlanetLib.instance.getLogger().info("\033[32mHook Vault successfully\033[0m");
    }
    public static double getBalance(@Nonnull OfflinePlayer player) {
        if(rsp==null) {
            Bukkit.getLogger().severe("Planetlib: Attempted to access Vault without proper initialization...");
            return 0;
        }
        return rsp.getProvider().getBalance(player);
    }
    public static void depositBalance(@Nonnull OfflinePlayer player, double balance) {
        if(rsp!=null) rsp.getProvider().depositPlayer(player, balance);
        else Bukkit.getLogger().severe("Planetlib: Attempted to access Vault without proper initialization...");
    }
    public static boolean withdrawBalance(@Nonnull OfflinePlayer player, double balance) {
        if(getBalance(player) < balance) return false;
        if(rsp!=null) rsp.getProvider().withdrawPlayer(player, balance);
        return true;
    }
    public static boolean haveBalance(@Nonnull OfflinePlayer player, double balance) {
        return getBalance(player) >= balance;
    }
    public static void setBalance(@Nonnull OfflinePlayer player, double balance) {
        if (getBalance(player) >= balance) {
            withdrawBalance(player, getBalance(player) - balance);
        } else {
            depositBalance(player, balance - getBalance(player));
        }
    }
}
