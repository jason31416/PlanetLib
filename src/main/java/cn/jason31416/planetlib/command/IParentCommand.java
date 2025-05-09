package cn.jason31416.planetlib.command;

public interface IParentCommand {
    void registerSubCommand(String name, ICommand command);
}
