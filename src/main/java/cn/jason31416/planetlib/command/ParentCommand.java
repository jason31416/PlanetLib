package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.message.Message;
import cn.jason31416.planetlib.message.StaticMessages;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ParentCommand implements ICommand, IParentCommand {
    @Getter
    public final Map<String, ICommand> subCommands = new HashMap<>();
    public ParentCommand(String name, IParentCommand parent){
        parent.registerSubCommand(name, this);
    }
    public ParentCommand(List<String> names, IParentCommand parent){
        for(String name: names){
            parent.registerSubCommand(name, this);
        }
    }
    @Override @Nullable
    public Message execute(ICommandContext context) {
        if(!context.args().isEmpty()){
            if(subCommands.containsKey(context.getArg(0))){
                return subCommands.get(context.getArg(0)).execute(context.getSubContext());
            }
            return StaticMessages.UNKNOWN_COMMAND;
        }
        return executeRaw(context);
    }
    public List<String> tabComplete(ICommandContext context) {
        if(context.args().size() == 1) {
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
    public void registerSubCommand(String name, ICommand command){
        subCommands.put(name, command);
    }
    @Nullable
    public abstract Message executeRaw(ICommandContext context);
}
