package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.PlanetLib;
import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.util.general.Pair;
import cn.jason31416.planetlib.util.general.ShitMountainException;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public abstract class RootCommand implements ICommand, IParentCommand {
    @Getter
    public Map<String, ICommand> subCommands = new HashMap<>();
    String name;
    boolean registered=false;
    public RootCommand(String name) {
        this.name = name;
    }
    public void register(){
        if(registered) return;
        PlanetLib.instance.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(name, new BasicCommand() {
                @Override
                public void execute(CommandSourceStack commandSourceStack, String[] args) {
                    onCommand(commandSourceStack.getSender(), args);
                }
                @Override
                public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args){
                    return onTabComplete(commandSourceStack.getSender(), args);
                }
            });
        });
        registered = true;
    }
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull String[] strings) {
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
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull String[] strings){
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

    public static class Builder {
        private Function<ICommandContext, Message> directExecutor;
        private String name;
        private Map<String, Pair<Function<ICommandContext, Message>, Function<ICommandContext, List<String>>>> subNodeCommands;
        private Map<String, Function<ICommandContext, Message>> subParentCommands;

        public Builder(String name){
            this.name = name;
        }
        public void setDirectExecutor(Function<ICommandContext, Message> ex){
            directExecutor = ex;
        }
        public void addCommandNode(String path, Function<ICommandContext, Message> ex, Function<ICommandContext, List<String>> tc){
            subNodeCommands.put(path, Pair.of(ex, tc));
        }
        public void addCommandNode(String path, Function<ICommandContext, Message> ex){
            subNodeCommands.put(path, Pair.of(ex, ctx->null));
        }
        public void setParentExecutor(String path, Function<ICommandContext, Message> ex){
            subParentCommands.put(path, ex);
        }

        public RootCommand build(){
            RootCommand ret = new RootCommand(name) {
                @Override
                public @Nullable Message execute(ICommandContext context) {
                    return directExecutor.apply(context);
                }
            };
            for(String i: subNodeCommands.keySet()){
                IParentCommand currentStep=ret;
                String curPath="";
                for(String j: Arrays.stream(i.split(" ")).toList().subList(0, i.split(" ").length-1)){
                    curPath += j;
                    if(currentStep.getSubCommands().containsKey(j)){
                        if(!(currentStep.getSubCommands().get(j) instanceof IParentCommand)){
                            throw new ShitMountainException("Error during building command: "+name+"! An registered command leaf node became another command's parent.");
                        }
                        currentStep = (IParentCommand) currentStep.getSubCommands().get(j);
                        continue;
                    }
                    Function<ICommandContext, Message> ex;
                    if(subParentCommands.containsKey(curPath)){
                        ex = subParentCommands.get(curPath);
                    }else ex=ctx->null;
                    new ParentCommand(j, currentStep) {
                        @Override
                        public @Nullable Message executeRaw(ICommandContext context) {
                            return ex.apply(context);
                        }
                    };
                    curPath += " ";
                }
                String j = i.split(" ")[i.split(" ").length-1];
                if(currentStep.getSubCommands().containsKey(j))
                    throw new ShitMountainException("Error during building command: "+name+"! An registered command leaf node became another command's parent or it was registered multiple times!");
                new ChildCommand(j, currentStep){
                    @Override
                    public @Nullable Message execute(ICommandContext context) {
                        return subNodeCommands.get(i).first().apply(context);
                    }

                    @Override
                    public List<String> tabComplete(ICommandContext context) {
                        return subNodeCommands.get(i).second().apply(context);
                    }
                };
            }
            ret.register();
            return ret;
        }
    }
}
