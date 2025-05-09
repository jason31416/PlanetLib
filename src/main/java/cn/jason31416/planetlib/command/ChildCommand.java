package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.message.Message;

import javax.annotation.Nullable;
import java.util.List;

public abstract class ChildCommand implements ICommand {
    public ChildCommand(String name, IParentCommand parent) {
        parent.registerSubCommand(name, this);
    }
    public ChildCommand(List<String> names, IParentCommand parent){
        for(String name: names){
            parent.registerSubCommand(name, this);
        }
    }
    @Nullable
    public abstract Message execute(ICommandContext context);

    public abstract List<String> tabComplete(ICommandContext context);
}
