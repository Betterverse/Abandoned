package net.betterverse.mobdisguise.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event data for when a player undisguises.
 * 
 * @author iffa
 * 
 */
public class UnDisguiseEvent extends Event implements Cancellable {
    private static final long serialVersionUID = -5100103933008602505L;
    private Player player;
    private boolean canceled;

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public UnDisguiseEvent(String event, Player player, boolean mob) {
        super(event);
        this.player = player;
    }

    /**
     * Gets the player associated with this event.
     * 
     * @return Player
     */
    public Player getPlayer() {
        return this.player;
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
