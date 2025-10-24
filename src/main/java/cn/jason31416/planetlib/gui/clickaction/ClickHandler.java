package cn.jason31416.planetlib.gui.clickaction;

import cn.jason31416.planetlib.gui.GUIRunnable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClickHandler annotation
 * Used to mark a method as a click handler for the GUI
 * The method should take exactly two parameter of types: {@link GUIRunnable.RunnableInvocation} invocation and {@link String} MapTree
 * and a return type of void (other is fine but the return value will be ignored)
 * The method should be public and not static.
 * Then, an object of the class should be created and registered to the GUI using {@link cn.jason31416.planetlib.gui.clickaction.RegisteredGUIRunnable#registerAll(Object)}
 *
 * @since 1.3.1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ClickHandler {
    String id();
}
