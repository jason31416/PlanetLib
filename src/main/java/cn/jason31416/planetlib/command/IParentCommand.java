package cn.jason31416.planetlib.command;

import java.util.Map;

public interface IParentCommand {
    void registerSubCommand(String name, ICommand command);
    Map<String, ICommand> getSubCommands();
}
