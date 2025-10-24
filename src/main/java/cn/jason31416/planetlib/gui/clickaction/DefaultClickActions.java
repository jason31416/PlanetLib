package cn.jason31416.planetlib.gui.clickaction;

import cn.jason31416.planetlib.gui.GUIRunnable;
import cn.jason31416.planetlib.gui.GUITemplate;
import cn.jason31416.planetlib.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryAction;

import java.util.Arrays;

/**
 * The default click actions for GUIs
 *
 * @since 1.3.1
 */
public class DefaultClickActions {
    // Base click actions

    @ClickHandler(id="close")
    public void closeAction(GUIRunnable.RunnableInvocation invocation, String[] args){
        invocation.getGui().close();
    }
    @ClickHandler(id="run")
    public void executeCommandAction(GUIRunnable.RunnableInvocation invocation, String[] args){
        String command = String.join(" ", args).replace("%player%", invocation.getGui().getPlayer().getName());
        Bukkit.getServer().dispatchCommand(invocation.getGui().getPlayer().getPlayer(), command);
    }
    @ClickHandler(id="open")
    public void openGUIAction(GUIRunnable.RunnableInvocation invocation, String[] args){
        String guiName = String.join(" ", args);
        invocation.getGui().getSession().display(GUITemplate.getGUI(guiName));
    }
    @ClickHandler(id="run-server")
    public void serverAction(GUIRunnable.RunnableInvocation invocation, String[] args){
        String command = String.join(" ", args).replace("%player%", invocation.getGui().getPlayer().getName());
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
    }
    @ClickHandler(id="message")
    public void messageAction(GUIRunnable.RunnableInvocation invocation, String[] args) {
        String message = String.join(" ", args);
        invocation.getGui().getPlayer().sendMessage(Message.of(message).add("player", invocation.getGui().getPlayer().getName()));
    }

    public static void runInvocation(GUIRunnable.RunnableInvocation invocation, String[] args){
        if(args.length < 1) throw new IllegalArgumentException("No action type specified for conditional click actions!");
        String actionType = args[0];
        String[] actionArgs = new String[args.length - 1];
        System.arraycopy(args, 1, actionArgs, 0, actionArgs.length);
        if(RegisteredGUIRunnable.getClickHandlers().containsKey(actionType)){
            RegisteredGUIRunnable.getClickHandlers().get(actionType).apply(actionArgs).run(invocation);
        }
    }

    // Conditional click actions
    @ClickHandler(id="right-click")
    public void rightClickAction(GUIRunnable.RunnableInvocation invocation, String[] args) {
        if(invocation.getAction() == InventoryAction.PICKUP_HALF) {
            runInvocation(invocation, args);
        }
    }
    @ClickHandler(id="left-click")
    public void leftClickAction(GUIRunnable.RunnableInvocation invocation, String[] args) {
        if(invocation.getAction() == InventoryAction.PICKUP_ALL) {
            runInvocation(invocation, args);
        }
    }
    @ClickHandler(id="shift-click")
    public void shiftClickAction(GUIRunnable.RunnableInvocation invocation, String[] args) {
        if (invocation.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            runInvocation(invocation, args);
        }
    }
    @ClickHandler(id="cost")
    public void CostAction(GUIRunnable.RunnableInvocation invocation, String[] args) {
        if (invocation.getGui().getPlayer().withdrawBalance(Double.parseDouble(args[0]))) {
            runInvocation(invocation, Arrays.copyOfRange(args, 1, args.length));
        }
    }
}
