package net.betterverse.towns.war.event;

import org.bukkit.event.Event;

import net.betterverse.towns.war.CellUnderAttack;
import org.bukkit.event.HandlerList;

public class CellWonEvent extends Event {
	private static final long serialVersionUID = 4691420283914184122L;
	private CellUnderAttack cellAttackData;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public CellWonEvent(CellUnderAttack cellAttackData) {
		super();
		this.cellAttackData = cellAttackData;
	}

	public CellUnderAttack getCellAttackData() {
		return cellAttackData;
	}
}
