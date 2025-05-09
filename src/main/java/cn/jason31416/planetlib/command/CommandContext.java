package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;

import java.util.List;

public class CommandContext implements ICommandContext {
    public final List<String> args;
    public final SimpleSender sender;
    public final SimplePlayer player;
    String commandName;
    public CommandContext(List<String> args, SimpleSender sender, SimplePlayer player, String commandName) {
        this.args = args;
        this.sender = sender;
        this.player = player;
        this.commandName = commandName;
    }
    @Override
    public ICommandContext getSubContext() {
        return new CommandContext(args.subList(1, args.size()), sender, player, commandName+ " " + args.get(0));
    }

    @Override
    public String getArg(int index) {
        if (index >= args.size()) {
            return "";
        }
        return args.get(index);
    }
    @Override
    public String getUsage(ParameterType... parameterTypes){
        StringBuilder usage = new StringBuilder();
        usage.append("/").append(commandName).append(" ");
        for (int i = 0; i < parameterTypes.length; i++) {
            ParameterType parameterType = parameterTypes[i];
            if (i > 0) {
                usage.append(" ");
            }
            usage.append(parameterType.getUsage());
        }
        return usage.toString();
    }

    @Override
    public SimplePlayer player() {
        return player;
    }

    @Override
    public SimpleSender sender() {
        return sender;
    }

    @Override
    public List<String> args() {
        return args;
    }
}
