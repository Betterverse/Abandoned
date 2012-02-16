package net.betterverse.mobdisguise.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * DisguiseEvent
 * 
 * @author iffa
 * 
 */
public class DisguiseEvent extends Event implements Cancellable {
    private static final long serialVersionUID = -6426402822588097606L;
    private Player player;
    private boolean canceled;

    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public DisguiseEvent(String event, Player player) {
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
        // TODO Auto-generated method stub
        return this.canceled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.canceled = cancel;
    }

}
