package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class RootCommand implements ICommand, IParentCommand, CommandExecutor, TabCompleter {
    public Map<String, ICommand> subCommands = new HashMap<>();
    String name;
    public RootCommand(String name) {
        this.name = name;
    }
    public void register(){
        PluginCommand cmd = Bukkit.getPluginCommand(name);
        if(cmd!= null){
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] strings) {
        CommandContext context = new CommandContext(Arrays.asList(strings), SimpleSender.of(commandSender), SimplePlayer.of(commandSender), name);
        if(context.args().isEmpty()){
            Message msg = execute(context);
            if(msg!= null) context.sender().sendMessage(msg);
            return true;
        }
        if(subCommands.containsKey(context.getArg(0))){
            ICommand subCommand = subCommands.get(context.getArg(0));
            Message msg = subCommand.execute(context.getSubContext());
            if(msg!= null) context.sender().sendMessage(msg);
        }else{
            context.sender().sendMessage(StaticMessages.UNKNOWN_COMMAND);
        }
        return true;
    }
    @Nullable
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] strings){
        if(strings.length == 0){
            return null;
        }
        CommandContext context = new CommandContext(Arrays.asList(strings), SimpleSender.of(commandSender), SimplePlayer.of(commandSender), name);
        return tabComplete(context);
    }
    public void registerSubCommand(String name, ICommand command){
        subCommands.put(name, command);
    }
    @Nullable
    public abstract Message execute(ICommandContext context);
    public List<String> tabComplete(ICommandContext context) {
        if(context.args().size()==1) {
            List<String> result = new ArrayList<>();
            for (String key : subCommands.keySet()) {
                if (key.startsWith(context.getArg(0))) {
                    result.add(key);
                }
            }
            return result;
        }else if(subCommands.containsKey(context.getArg(0))){
            ICommand subCommand = subCommands.get(context.getArg(0));
            return subCommand.tabComplete(context.getSubContext());
        }else{
            return null;
        }
    }
}
