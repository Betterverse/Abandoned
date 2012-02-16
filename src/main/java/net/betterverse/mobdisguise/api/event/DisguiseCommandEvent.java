package net.betterverse.mobdisguise.api.event;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event data for when the MobDisguise command is used.
 * 
 * @author iffa
 * 
 */
public class DisguiseCommandEvent extends Event implements Cancellable {
    private static final long serialVersionUID = -1970653423890974618L;
    private CommandSender sender;
    private String[] args;
    private boolean canceled;

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public DisguiseCommandEvent(String event, CommandSender sender, String[] args) {
        super(event);
        this.sender = sender;
        this.args = args;
    }

    /**
     * Gets the commandsender.
     * 
     * @return CommandSender
     */
    public CommandSender getSender() {
        return this.sender;
    }

    /**
     * Gets the command arguments.
     * 
     * @return Args
     */
    public String[] getArgs() {
        return this.args;
    }

    @Override
    public boolean isCancelled() {
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }

}
