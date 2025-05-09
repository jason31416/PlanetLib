package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.message.Message;

import javax.annotation.Nullable;
import java.util.List;

public interface ICommand {
    @Nullable Message execute(ICommandContext context);
    List<String> tabComplete(ICommandContext context);
}
