package net.betterverse.towns.war.event;

import org.bukkit.event.Event;

import net.betterverse.towns.war.CellUnderAttack;
import org.bukkit.event.HandlerList;

public class CellAttackCancelledEvent extends Event {
	private static final long serialVersionUID = 2036661065011346448L;
	private CellUnderAttack cell;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
	public CellAttackCancelledEvent(CellUnderAttack cell) {
		super();
		this.cell = cell;
	}

	public CellUnderAttack getCell() {
		return cell;
	}
}
