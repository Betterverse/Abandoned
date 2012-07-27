package net.betterverse.towns.event;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.material.Attachable;

import net.betterverse.towns.ChunkNotification;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.PlayerCache;
import net.betterverse.towns.PlayerCache.TownBlockStatus;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.command.TownCommand;
import net.betterverse.towns.command.TownsCommand;
import net.betterverse.towns.object.BlockLocation;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.TownsPermission;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.war.TownsWarConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Handle events for all Player related events
 *
 * @author Shade
 */
public class TownsPlayerListener implements Listener {
    
	private final Towns plugin;

	public TownsPlayerListener(Towns plugin) {
		this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		try {
			plugin.getTownsUniverse().onLogin(player);
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerQuit(PlayerQuitEvent event) {
		plugin.getTownsUniverse().onLogout(event.getPlayer());

		// Remove from teleport queue (if exists)
		try {
			if (plugin.getTownsUniverse().isTeleportWarmupRunning()) {
				plugin.getTownsUniverse().abortTeleportRequest(plugin.getTownsUniverse().getResident(event.getPlayer().getName().toLowerCase()));
			}
		} catch (NotRegisteredException e) {
		}

		plugin.deleteCache(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerRespawn(PlayerRespawnEvent event) {
		Player player = event.getPlayer();
		TownsMessaging.sendDebugMsg("onPlayerDeath: " + player.getName());
		if (TownsSettings.isTownRespawning()) {
			try {
				Location respawn = plugin.getTownsUniverse().getTownSpawnLocation(player);
				event.setRespawnLocation(respawn);
			} catch (TownsException e) {
				// Not set will make it default.
			}
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerInteract(PlayerInteractEvent event) {
		//System.out.println("onPlayerInteract2");
		//long start = System.currentTimeMillis();

		if (event.isCancelled()) {
			// Fix for bucket bug.
			if (event.getAction() == Action.RIGHT_CLICK_AIR) {
				Integer item = event.getPlayer().getItemInHand().getTypeId();
				// block cheats for placing water/lava/fire/lighter use.
				if (item == 326 || item == 327 || item == 259 || (item >= 8 && item <= 11) || item == 51) {
					event.setCancelled(true);
				}
			}
			return;
		}

		Block block = event.getPlayer().getLocation().getBlock().getRelative(BlockFace.DOWN);
		TownsWorld townsWorld = null;

		try {
			townsWorld = TownsUniverse.getWorld(block.getLocation().getWorld().getName());
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// prevent players trampling crops

		if ((event.getAction() == Action.PHYSICAL) && (townsWorld.isUsingTowns())) {
			if ((block.getType() == Material.SOIL) || (block.getType() == Material.CROPS)) {
				if (townsWorld.isDisablePlayerTrample()) {
					event.setCancelled(true);
					return;
				}
			}
		}

		// Towns regen
		if (TownsSettings.getRegenDelay() > 0) {
			if (event.getClickedBlock().getState().getData() instanceof Attachable) {
				Attachable attachable = (Attachable) event.getClickedBlock().getState().getData();
				BlockLocation attachedToBlock = new BlockLocation(event.getClickedBlock().getRelative(attachable.getAttachedFace()).getLocation());
				// Prevent attached blocks from falling off when interacting
				if (plugin.getTownsUniverse().hasProtectionRegenTask(attachedToBlock)) {
					event.setCancelled(true);
				}
			}
		}

		if (event.hasItem()) {
			if (TownsSettings.isItemUseId(event.getItem().getTypeId())) {
				onPlayerInteractEvent(event);
				return;
			}
		}
		// fix for minequest causing null block interactions.
		if (event.getClickedBlock() != null) {
			if (TownsSettings.isSwitchId(event.getClickedBlock().getTypeId()) || event.getAction() == Action.PHYSICAL) {
				onPlayerSwitchEvent(event, null);
				return;
			}
		}
		//plugin.sendDebugMsg("onPlayerItemEvent took " + (System.currentTimeMillis() - start) + "ms");
		//}
	}

	public void onPlayerInteractEvent(PlayerInteractEvent event) {
		Player player = event.getPlayer();

		Block block = event.getClickedBlock();
		WorldCoord worldCoord;
		//System.out.println("onPlayerInteractEvent");

		try {
			if (block != null) {
				worldCoord = new WorldCoord(TownsUniverse.getWorld(player.getWorld().getName()), Coord.parseCoord(block));
			} else {
				worldCoord = new WorldCoord(TownsUniverse.getWorld(player.getWorld().getName()), Coord.parseCoord(player));
			}
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
			event.setCancelled(true);
			return;
		}

		//Get itemUse permissions (updates if none exist)
		boolean bItemUse;

		if (block != null) {
			bItemUse = TownsUniverse.getCachePermissions().getCachePermission(player, block.getLocation(), TownsPermission.ActionType.ITEM_USE);
		} else {
			bItemUse = TownsUniverse.getCachePermissions().getCachePermission(player, player.getLocation(), TownsPermission.ActionType.ITEM_USE);
		}

		PlayerCache cache = plugin.getCache(player);
		//cache.updateCoord(worldCoord);
		try {
			TownBlockStatus status = cache.getStatus();
			if (status == TownBlockStatus.UNCLAIMED_ZONE && plugin.hasWildOverride(worldCoord.getWorld(), player, event.getItem().getTypeId(), TownsPermission.ActionType.ITEM_USE)) {
				return;
			}
			if (status == TownBlockStatus.WARZONE) {
				if (! TownsWarConfig.isAllowingItemUseInWarZone()) {
					event.setCancelled(true);
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_warzone_cannot_use_item"));
				}
				return;
			}
			if (! bItemUse) {
				event.setCancelled(true);
			}
			if (cache.hasBlockErrMsg()) {
				TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
			}
		} catch (NullPointerException e) {
			System.out.print("NPE generated!");
			System.out.print("Player: " + event.getPlayer().getName());
			System.out.print("Item: " + event.getItem().getType().toString());
			//System.out.print("Block: " + block.getType().toString());
		}
	}

	public void onPlayerSwitchEvent(PlayerInteractEvent event, String errMsg) {
		Player player = event.getPlayer();
		Block block = event.getClickedBlock();

		if (! TownsSettings.isSwitchId(block.getTypeId())) {
			return;
		}

		WorldCoord worldCoord;
		try {
			worldCoord = new WorldCoord(TownsUniverse.getWorld(block.getWorld().getName()), Coord.parseCoord(block));
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
			event.setCancelled(true);
			return;
		}

		//Get switch permissions (updates if none exist)
		boolean bSwitch = TownsUniverse.getCachePermissions().getCachePermission(player, block.getLocation(), TownsPermission.ActionType.SWITCH);

		PlayerCache cache = plugin.getCache(player);
		//cache.updateCoord(worldCoord);
		TownBlockStatus status = cache.getStatus();
		if (status == TownBlockStatus.UNCLAIMED_ZONE && plugin.hasWildOverride(worldCoord.getWorld(), player, block.getTypeId(), TownsPermission.ActionType.SWITCH)) {
			return;
		}
		if (status == TownBlockStatus.WARZONE) {
			if (! TownsWarConfig.isAllowingSwitchesInWarZone()) {
				event.setCancelled(true);
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_warzone_cannot_use_switches"));
			}
			return;
		}
		if (! bSwitch) {
			event.setCancelled(true);
		}
		if (cache.hasBlockErrMsg()) {
			TownsMessaging.sendErrorMsg(player, cache.getBlockErrMsg());
		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void playerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location from;
		try {
			from = plugin.getCache(player).getLastLocation();
		} catch (NullPointerException e) {
			from = event.getFrom();
		}
		Location to = event.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
			return;
		}

		// Prevent fly/double jump cheats
		try {
			if (TownsUniverse.getWorld(player.getWorld().getName()).isUsingTowns()) {
				if (TownsSettings.isUsingCheatProtection() && ! TownsUniverse.getPermissionSource().hasPermission(player, "towns.cheat.bypass")) {
					if (!event.getEventName().equals("PLAYER_TELEPORT") && from.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR && player.getFallDistance() == 0 && player.getVelocity().getY() <= - 0.6 && (player.getLocation().getY() > 0)) {
						//plugin.sendErrorMsg(player, "Cheat Detected!");

						Location blockLocation = from;

						//find the first non air block below us
						while ((blockLocation.getBlock().getType() == Material.AIR) && (blockLocation.getY() > 0)) {
							blockLocation.setY(blockLocation.getY() - 1);
						}

						// set to 1 block up so we are not sunk in the ground
						blockLocation.setY(blockLocation.getY() + 1);

						plugin.getCache(player).setLastLocation(blockLocation);
						player.teleport(blockLocation);
						return;
					}
				}
			}
		} catch (NotRegisteredException e1) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
			return;
		}

		try {
			TownsWorld fromWorld = TownsUniverse.getWorld(from.getWorld().getName());
			WorldCoord fromCoord = new WorldCoord(fromWorld, Coord.parseCoord(from));
			TownsWorld toWorld = TownsUniverse.getWorld(to.getWorld().getName());
			WorldCoord toCoord = new WorldCoord(toWorld, Coord.parseCoord(to));
			if (! fromCoord.equals(toCoord)) {
				onPlayerMoveChunk(player, fromCoord, toCoord, from, to);
			} else {
				//plugin.sendDebugMsg("	From: " + fromCoord);
				//plugin.sendDebugMsg("	To:   " + toCoord);
				//plugin.sendDebugMsg("		" + from.toString());
				//plugin.sendDebugMsg("		" + to.toString());
			}
		} catch (NotRegisteredException e) {
			TownsMessaging.sendErrorMsg(player, e.getError());
		}

		plugin.getCache(player).setLastLocation(to);
		//plugin.sendDebugMsg("onBlockMove: " + player.getName() + ": ");
		//plugin.sendDebugMsg("		" + from.toString());
		//plugin.sendDebugMsg("		" + to.toString());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		playerMove(event);
	}

	public void onPlayerMoveChunk(Player player, WorldCoord from, WorldCoord to, Location fromLoc, Location toLoc) {
		//plugin.sendDebugMsg("onPlayerMoveChunk: " + player.getName());

		plugin.getCache(player).setLastLocation(toLoc);
		plugin.getCache(player).updateCoord(to);

		// TODO: Player mode
		if (plugin.hasPlayerMode(player, "townclaim")) {
			TownCommand.parseTownClaimCommand(player, new String[]{});
		}
		if (plugin.hasPlayerMode(player, "townunclaim")) {
			TownCommand.parseTownUnclaimCommand(player, new String[]{});
		}
		if (plugin.hasPlayerMode(player, "map")) {
			TownsCommand.showMap(player);
		}

		// claim: attempt to claim area
		// claim remove: remove area from town

		// Check if player has entered a new town/wilderness
		if (to.getWorld().isUsingTowns() && TownsSettings.getShowTownNotifications()) {
			ChunkNotification chunkNotifier = new ChunkNotification(from, to);
			String msg = chunkNotifier.getNotificationString();
			if (msg != null) {
				player.sendMessage(msg);
			}
		}
	}
}
