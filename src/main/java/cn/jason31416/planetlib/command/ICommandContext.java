package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.message.StaticMessages;
import cn.jason31416.planetlib.wrapper.SimplePlayer;
import cn.jason31416.planetlib.wrapper.SimpleSender;

import java.util.List;

public interface ICommandContext {
    String getUsage(ParameterType... parameterTypes);
    SimplePlayer player();
    SimpleSender sender();
    List<String> args();
    String getArg(int index);
    ICommandContext getSubContext();
    default int getIntArg(int index){
        Object result = ParameterType.INTEGER.handle(getArg(index));
        if(result==null) return 0;
        return (int)result;
    }
    default double getDoubleArg(int index){
        Object result = ParameterType.DOUBLE.handle(getArg(index));
        if(result==null) return 0;
        return (double)result;
    }
    default SimplePlayer getPlayerArg(int index) {
        Object result = ParameterType.PLAYER.handle(getArg(index));
        if (result == null) return null;
        return (SimplePlayer) result;
    }
    default boolean checkArgs(ParameterType... parameterTypes){
        if(args().size() < parameterTypes.length) {
            sender().sendMessage(StaticMessages.INCORRECT_USAGE.add("usage", getUsage(parameterTypes)));
            return false;
        }
        for(int i = 0; i < parameterTypes.length; i++){
            if(parameterTypes[i].checker.handle(getArg(i))==null){
                sender().sendMessage(StaticMessages.INCORRECT_USAGE.add("usage", getUsage(parameterTypes)));
                return false;
            }
        }
        return true;
    }
    default int getCurrentArg(){
        if(args().isEmpty()) return 1;
        return args().size();
    }
    default SimplePlayer getPlayer(){
        return player();
    }
    default SimpleSender getSender(){
        return sender();
    }
}
