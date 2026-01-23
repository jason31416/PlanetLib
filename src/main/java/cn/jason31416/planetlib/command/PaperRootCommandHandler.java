package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.PlanetLib;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class PaperRootCommandHandler {
    protected static void register(RootCommand rootCommand) {
        PlanetLib.instance.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(rootCommand.name, rootCommand.aliases, new BasicCommand() {
                @Override
                public void execute(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args) {
                    rootCommand.onCommand(commandSourceStack.getSender(), args);
                }
                @Override
                public @NotNull Collection<String> suggest(@NotNull CommandSourceStack commandSourceStack, String @NotNull [] args){
                    return rootCommand.onTabComplete(commandSourceStack.getSender(), args);
                }
            });
        });
    }
}
