package cn.jason31416.planetlib.command;

import cn.jason31416.planetlib.wrapper.SimplePlayer;

public enum ParameterType {
    STRING((input) -> input, "<String>"),
    INTEGER((input) -> {
        try {
            return Integer.parseInt(input);
        }catch(NumberFormatException e) {
            return null;
        }
    }, "<Integer>"),
    DOUBLE((input) -> {
        try {
            return Double.parseDouble(input);
        }catch(NumberFormatException e) {
            return null;
        }
    }, "<Number>"),
    PLAYER(SimplePlayer::of, "<Player>");
    public static interface ParameterChecker {
        Object handle(String input);
    }
    public final ParameterChecker checker;
    public final String usage;
    ParameterType(ParameterChecker checker, String usage) {
        this.checker = checker;
        this.usage = usage;
    }
    public String getUsage() {
        return usage;
    }
    public Object handle(String input) {
        return checker.handle(input);
    }
}
