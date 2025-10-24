package cn.jason31416.planetlib.gui.clickaction;

import cn.jason31416.planetlib.gui.GUIRunnable;
import cn.jason31416.planetlib.util.general.Provider;
import lombok.Getter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Registering GUI click handlers that might be used in multiple GUIs
 * They may accept a String[] as parameter.
 *
 * @since 1.3.1
 */
public abstract class RegisteredGUIRunnable implements GUIRunnable {
    @Getter
    private static final Map<String, Function<String[], RegisteredGUIRunnable>> clickHandlers = new HashMap<>();
    public final String id;
    public final String[] args;
    public RegisteredGUIRunnable(String id, String[] args) {
        this.id = id;
        this.args = args;
    }
    public static void register(String id, Function<String[], RegisteredGUIRunnable> provider) {
        clickHandlers.put(id, provider);
    }
    @SneakyThrows
    public static void registerAll(Object obj){
        for(Method i: obj.getClass().getDeclaredMethods()){
            i.setAccessible(true);
            ClickHandler cl = i.getAnnotation(ClickHandler.class);
            if(cl != null){
                register(cl.id(), argsl -> new RegisteredGUIRunnable(cl.id(), argsl) {
                    @Override
                    public void run(RunnableInvocation invocation) {
                        try {
                            i.invoke(obj, invocation, args);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }
}
