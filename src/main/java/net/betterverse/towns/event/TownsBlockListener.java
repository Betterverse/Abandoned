package net.betterverse.towns.event;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.PlayerCache;
import net.betterverse.towns.PlayerCache.TownBlockStatus;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.BlockLocation;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.NeedsPlaceholder;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockType;
import net.betterverse.towns.object.TownsPermission;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.ProtectionRegenTask;
import net.betterverse.towns.war.TownsWar;
import net.betterverse.towns.war.TownsWarConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsBlockListener implements Listener {

	private final Towns plugin;

	public TownsBlockListener(Towns plugin) {
		this.plugin = plugin;
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPhysics(BlockPhysicsEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		//long start = System.currentTimeMillis();

		Block block = event.getBlock();
		BlockLocation blockLocation = new BlockLocation(block.getLocation());

		// if this is a placeholder remove it, as it's no longer needed.
		if (plugin.getTownsUniverse().isPlaceholder(block)) {
			plugin.getTownsUniverse().removePlaceholder(block);
			block.setTypeId(0, false);
		}

		if (plugin.getTownsUniverse().hasProtectionRegenTask(blockLocation)) {
			//Cancel any physics events as we will be replacing this block
			event.setCancelled(true);
		} else {
			// Check the block below and cancel the event if that block is going to be replaced.		
			Block blockBelow = block.getRelative(BlockFace.DOWN);
			blockLocation = new BlockLocation(blockBelow.getLocation());

			if (plugin.getTownsUniverse().hasProtectionRegenTask(blockLocation) && (NeedsPlaceholder.contains(block.getType()))) {
				//System.out.print("Cancelling for Below on - " + block.getType().toString());
				event.setCancelled(true);
			}
		}

		//plugin.sendDebugMsg("onBlockPhysics took " + (System.currentTimeMillis() - start) + "ms ("+event.isCancelled() +")");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockBreak(BlockBreakEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		//long start = System.currentTimeMillis();

		Player player = event.getPlayer();
		Block block = event.getBlock();
		WorldCoord worldCoord;
		try {
			worldCoord = new WorldCoord(TownsUniverse.getWorld(block.getWorld().getName()), Coord.parseCoord(block));

			//Get build permissions (updates if none exist)
			boolean bDestroy = TownsUniverse.getCachePermissions().getCachePermission(player, block.getLocation(), TownsPermission.ActionType.DESTROY);

			PlayerCache cache = plugin.getCache(player);
			TownBlockStatus status = cache.getStatus();

			if (((status == TownBlockStatus.UNCLAIMED_ZONE) && (plugin.hasWildOverride(worldCoord.getWorld(), player, event.getBlock().getTypeId(), TownsPermission.ActionType.DESTROY))) || ((status == TownBlockStatus.TOWN_RESIDENT) && (plugin.getTownsUniverse().getTownBlock(block.getLocation()).getType() == TownBlockType.WILDS) && (plugin.hasWildOverride(worldCoord.getWorld(), player, event.getBlock().getTypeId(), TownsPermission.ActionType.DESTROY)))) {
				return;
			}
			if (status == TownBlockStatus.WARZONE) {
				if (!TownsWarConfig.isEditableMaterialInWarZone(block.getType())) {
					event.setCancelled(true);
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_warzone_cannot_edit_material"), "destroy", block.getType().toString().toLowerCase()));
				}
				return;
			}
			if (!bDestroy) {
				long delay = TownsSettings.getRegenDelay();
				if (delay > 0) {
					if (!plugin.getTownsUniverse().isPlaceholder(block)) {
						if (!plugin.getTownsUniverse().hasProtectionRegenTask(new BlockLocation(block.getLocation()))) {
							ProtectionRegenTask task = new ProtectionRegenTask(plugin.getTownsUniverse(), block, true);
							task.setTaskId(plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task, 20 * delay));
							plugin.getTownsUniverse().addProtectionRegenTask(task);
						}
					} else {
						plugin.getTownsUniverse().removePlaceholder(block);
						block.setTypeId(0, false);
					}
				}
				event.setCancelled(true);
			}

			if ((cache.hasBlockErrMsg()) && (event.isCancelled())) {
				TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
			}
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
		}

		//plugin.sendDebugMsg("onBlockBreakEvent took " + (System.currentTimeMillis() - start) + "ms ("+event.getPlayer().getName()+", "+event.isCancelled() +")");
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPlace(BlockPlaceEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		//long start = System.currentTimeMillis();

		Player player = event.getPlayer();
		Block block = event.getBlock();
		WorldCoord worldCoord;
		try {
			worldCoord = new WorldCoord(TownsUniverse.getWorld(block.getWorld().getName()), Coord.parseCoord(block));

			//Get build permissions (updates if none exist)
			boolean bBuild = TownsUniverse.getCachePermissions().getCachePermission(player, block.getLocation(), TownsPermission.ActionType.BUILD);

			PlayerCache cache = plugin.getCache(player);
			TownBlockStatus status = cache.getStatus();
			if (TownsSettings.isBypassMaterial(block.getType())) {
				switch (status) {
					case UNCLAIMED_ZONE:
					case ADMIN:
					case WARZONE:
					case OFF_WORLD:
					case UNKOWN:
					case NOT_REGISTERED:
						break;
					default:
						return;
				}
			}
			if (((status == TownBlockStatus.UNCLAIMED_ZONE) && (plugin.hasWildOverride(worldCoord.getWorld(), player, event.getBlock().getTypeId(), TownsPermission.ActionType.BUILD))) || ((status == TownBlockStatus.TOWN_RESIDENT) && (plugin.getTownsUniverse().getTownBlock(block.getLocation()).getType() == TownBlockType.WILDS) && (plugin.hasWildOverride(worldCoord.getWorld(), player, event.getBlock().getTypeId(), TownsPermission.ActionType.BUILD)))) {
				return;
			}

			if ((status == TownBlockStatus.ENEMY && TownsWarConfig.isAllowingAttacks()) && event.getBlock().getType() == TownsWarConfig.getFlagBaseMaterial()) {
				//&& plugin.hasPlayerMode(player, "warflag")) {
				try {
					if (TownsWar.callAttackCellEvent(plugin, player, block, worldCoord)) {
						return;
					}
				} catch (TownsException e) {
					TownsMessaging.sendErrorMsg(player, e.getMessage());
				}

				event.setBuild(false);
				event.setCancelled(true);
			} else if (status == TownBlockStatus.WARZONE) {
				if (!TownsWarConfig.isEditableMaterialInWarZone(block.getType())) {
					event.setBuild(false);
					event.setCancelled(true);
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_warzone_cannot_edit_material"), "build", block.getType().toString().toLowerCase()));
				}
				return;
			} else {
				if (!bBuild) {
					event.setBuild(false);
					event.setCancelled(true);
				}
			}

			if ((cache.hasBlockErrMsg()) && (event.isCancelled())) {
				TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
			}
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
			event.setCancelled(true);
		}

		//plugin.sendDebugMsg("onBlockPlacedEvent took " + (System.currentTimeMillis() - start) + "ms ("+event.getPlayer().getName()+", "+event.isCancelled() +")");
	}

	// prevent blocks igniting if within a protected town area when fire spread is set to off.
	@EventHandler(priority = EventPriority.NORMAL)
	public void blockBurn(BlockBurnEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		if (onBurn(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockIgnite(BlockIgniteEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		if (onBurn(event.getBlock())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPistonRetract(BlockPistonRetractEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		//fetch the piston base
		Block block = event.getBlock();

		if (block.getType() != Material.PISTON_STICKY_BASE) {
			return;
		}

		//Get the block attached to the PISTON_EXTENSION of the PISTON_STICKY_BASE
		block = block.getRelative(event.getDirection()).getRelative(event.getDirection());

		if ((block.getType() != Material.AIR) && (!block.isLiquid())) {
			//check the block to see if it's going to pass a plot boundary
			if (testBlockMove(block, event.getDirection().getOppositeFace())) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void blockPistonExtend(BlockPistonExtendEvent event) {
		if (event.isCancelled()) {
			event.setCancelled(true);
			return;
		}

		List<Block> blocks = event.getBlocks();

		if (!blocks.isEmpty()) {
			//check each block to see if it's going to pass a plot boundary
			for (Block block : blocks) {
				if (testBlockMove(block, event.getDirection())) {
					event.setCancelled(true);
				}
			}
		}
	}

	private boolean testBlockMove(Block block, BlockFace direction) {
		Block blockTo = block.getRelative(direction);
		Location loc = block.getLocation();
		Location locTo = blockTo.getLocation();
		Coord coord = Coord.parseCoord(loc);
		Coord coordTo = Coord.parseCoord(locTo);

		TownsWorld townsWorld = null;
		TownBlock CurrentTownBlock = null, destinationTownBlock = null;

		try {
			townsWorld = TownsUniverse.getWorld(loc.getWorld().getName());
			CurrentTownBlock = townsWorld.getTownBlock(coord);
		} catch (NotRegisteredException e) {
			//System.out.print("Failed to fetch TownBlock");
		}

		try {
			destinationTownBlock = townsWorld.getTownBlock(coordTo);
		} catch (NotRegisteredException e1) {
			//System.out.print("Failed to fetch TownBlockTo");
		}

		if (CurrentTownBlock != destinationTownBlock) {
			// Cancel if either is not null, but other is (wild to town).
			if ((CurrentTownBlock == null && destinationTownBlock != null) || (CurrentTownBlock != null && destinationTownBlock == null)) {
				//event.setCancelled(true);
				return true;
			}

			// If both blocks are owned by the town.
			if (!CurrentTownBlock.hasResident() && !destinationTownBlock.hasResident()) {
				return false;
			}

			try {
				if ((!CurrentTownBlock.hasResident() && destinationTownBlock.hasResident()) || (CurrentTownBlock.hasResident() && !destinationTownBlock.hasResident()) || (CurrentTownBlock.getResident() != destinationTownBlock.getResident())
								|| (CurrentTownBlock.getPlotPrice() != - 1) || (destinationTownBlock.getPlotPrice() != - 1)) {
					return true;
				}
			} catch (NotRegisteredException e) {
				// Failed to fetch a resident
				return true;
			}
		}

		return false;
	}

	private boolean onBurn(Block block) {
		Location loc = block.getLocation();
		Coord coord = Coord.parseCoord(loc);

		try {
			TownsWorld townsWorld = TownsUniverse.getWorld(loc.getWorld().getName());

			if (townsWorld.isWarZone(coord)) {
				if (TownsWarConfig.isAllowingFireInWarZone()) {
					return false;
				} else {
					TownsMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getTypeId() + " from igniting within " + coord.toString() + ".");
					return true;
				}
			}

			TownBlock townBlock = townsWorld.getTownBlock(coord);
			if (townsWorld.isUsingTowns()) {
				if ((block.getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN && !townBlock.getTown().isFire() && !townsWorld.isForceFire() && !townBlock.getPermissions().fire) || (block.getRelative(BlockFace.DOWN).getType() != Material.OBSIDIAN && plugin.getTownsUniverse().isWarTime() && !townBlock.getTown().hasNation())) {
					TownsMessaging.sendDebugMsg("onBlockIgnite: Canceled " + block.getTypeId() + " from igniting within " + coord.toString() + ".");
					return true;
				}
			}
		} catch (TownsException x) {
		}

		return false;
	}
}
