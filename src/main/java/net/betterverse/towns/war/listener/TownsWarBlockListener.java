package net.betterverse.towns.war.listener;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import net.betterverse.towns.Towns;
import net.betterverse.towns.war.TownsWar;
import net.betterverse.towns.war.TownsWarConfig;
import net.betterverse.towns.war.event.CellAttackEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsWarBlockListener implements Listener {
    
	private final Towns plugin;

	public TownsWarBlockListener(Towns plugin) {
		this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	/**
	 * For Testing purposes only.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPlace(BlockPlaceEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlockPlaced();

		if (block == null) {
			return;
		}

		if (block.getType() == TownsWarConfig.getFlagBaseMaterial()) {
			int topY = block.getWorld().getHighestBlockYAt(block.getX(), block.getZ()) - 1;
			if (block.getY() >= topY) {
				CellAttackEvent cellAttackEvent = new CellAttackEvent(player, block);
				this.plugin.getServer().getPluginManager().callEvent(cellAttackEvent);
				if (cellAttackEvent.isCancelled()) {
					event.setBuild(false);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockBreak(BlockBreakEvent event) {
		TownsWar.checkBlock(event.getPlayer(), event.getBlock(), event);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockBurn(BlockBurnEvent event) {
		TownsWar.checkBlock(null, event.getBlock(), event);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPistonExtend(BlockPistonExtendEvent event) {
		for (Block block : event.getBlocks()) {
			TownsWar.checkBlock(null, block, event);
		}
	}

	/**
	 * TODO: Need to check if a immutable block is being moved with a sticky piston.
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPistonRetract(BlockPistonRetractEvent event) {
	}
}
