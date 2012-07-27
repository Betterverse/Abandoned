package net.betterverse.towns.war.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import net.betterverse.towns.war.Cell;
import org.bukkit.event.HandlerList;

public class CellDefendedEvent extends Event {
	private static final long serialVersionUID = 257333278929768100L;
	private Player player;
	private Cell cell;
    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }

	public CellDefendedEvent(Player player, Cell cell) {
		super();
		this.player = player;
		this.cell = cell;
	}

	public Player getPlayer() {
		return player;
	}

	public Cell getCell() {
		return cell;
	}
}
