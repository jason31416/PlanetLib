package cn.jason31416.planetlib.message;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public enum StaticMessages implements Message {
    UNKNOWN_COMMAND("<red>该命令不存在!"),
    INCORRECT_USAGE("<red>命令格式错误! 正确用法: %usage%"),
    NO_API_COMMAND("<red>该命令为API用命令, 无法直接使用!"),
    FAILED_TO_SAVE_DATA("\033[31mPlanetLib: 保存数据失败!\033[0m"),
    FAILED_TO_LOAD_DATA("\033[31mPlanetLib: 加载数据失败!\033[0m"),
    ERROR_GUI_CONFIG("\033[31mPlanetLib: 错误的GUI配置 %file%:%key%!\033[0m"),
    UNKNOWN_GUI_ITEM("\033[31mPlanetLib: GUI内的不可移动物品被移动到了GUI外!\033[0m");
    final Message message;
    StaticMessages(String message){
        this.message = new StringMessage(message);
    }

    @Override
    public StaticMessages add(String key, Object value) {
        message.add(key, value);
        return this;
    }
    public String toString(){
        return message.toString();
    }

    @Override
    public void send(CommandSender sender) {
        message.send(sender);
    }

    @Override
    public void sendActionbar(Player sender) {
        message.sendActionbar(sender);
    }

    public void sendConsole(){
        Bukkit.getLogger().info(message.toString());
    }
    public static void debug(String message){
        Bukkit.getLogger().info("\033[33m[PlanetLib Debug] \033[0m" + message);
    }
}
