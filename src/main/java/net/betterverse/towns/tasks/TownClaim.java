package net.betterverse.towns.tasks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.Player;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.PlotBlockData;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsRegenAPI;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;

/**
 * @author ElgarL
 */
public class TownClaim extends Thread {
	Towns plugin;
	volatile Player player;
	volatile Town town;
	List<WorldCoord> selection;
	boolean claim, forced;

	/**
	 * @param plugin	reference to towns
	 * @param player	Doing the claiming, or null
	 * @param town	  The claiming town
	 * @param selection List of WoorldCoords to claim/unclaim
	 * @param claim	 or unclaim
	 * @param forced	admin forced claim/unclaim
	 */
	public TownClaim(Towns plugin, Player player, Town town, List<WorldCoord> selection, boolean claim, boolean forced) {
		super();
		this.plugin = plugin;
		this.player = player;
		this.town = town;
		this.selection = selection;
		this.claim = claim;
		this.forced = forced;
		this.setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		List<TownsWorld> worlds = new ArrayList<TownsWorld>();
		List<Town> towns = new ArrayList<Town>();
		TownsWorld world;

		if (player != null) {
			TownsMessaging.sendMsg(player, "Processing " + ((claim) ? "Town Claim..." : "Town unclaim..."));
		}

		if (selection != null) {
			for (WorldCoord worldCoord : selection) {
				try {
					world = TownsUniverse.getWorld(worldCoord.getWorld().getName());
					if (! worlds.contains(world)) {
						worlds.add(world);
					}

					if (claim) {
						townClaim(town, worldCoord);
					} else {
						this.town = worldCoord.getTownBlock().getTown();
						townUnclaim(town, worldCoord, forced);
					}

					if (! towns.contains(town)) {
						towns.add(town);
					}
				} catch (NotRegisteredException e) {
					// Invalid world
					TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
				} catch (TownsException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			}
		} else if (! claim) {
			townUnclaimAll(town);
		}

		if (! towns.isEmpty()) {
			for (Town test : towns) {
				TownsUniverse.getDataSource().saveTown(test);
			}
		}

		if (! worlds.isEmpty()) {
			for (TownsWorld test : worlds) {
				TownsUniverse.getDataSource().saveWorld(test);
			}
		}

		plugin.updateCache();

		if (player != null) {
			if (claim) {
				TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_annexed_area"), (selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
			} else if (forced) {
				TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_admin_unclaim_area"), (selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
			}
		}
	}

	private void townClaim(Town town, WorldCoord worldCoord) throws TownsException {
		try {
			TownBlock townBlock = worldCoord.getTownBlock();
			try {
				throw new AlreadyRegisteredException(String.format(TownsSettings.getLangString("msg_already_claimed"), townBlock.getTown().getName()));
			} catch (NotRegisteredException e) {
				throw new AlreadyRegisteredException(TownsSettings.getLangString("msg_already_claimed_2"));
			}
		} catch (NotRegisteredException e) {
			TownBlock townBlock = worldCoord.getWorld().newTownBlock(worldCoord);
			townBlock.setTown(town);
			if (! town.hasHomeBlock()) {
				town.setHomeBlock(townBlock);
			}

			// Set the plot permissions to mirror the towns.
			townBlock.setType(townBlock.getType());
			TownsUniverse.getDataSource().saveTownBlock(townBlock);

			if (town.getWorld().isUsingPlotManagementRevert()) {
				PlotBlockData plotChunk = TownsRegenAPI.getPlotChunk(townBlock);
				if (plotChunk != null) {
					TownsRegenAPI.deletePlotChunk(plotChunk); // just claimed so stop regeneration.
				} else {
					plotChunk = new PlotBlockData(townBlock); // Not regenerating so create a new snapshot.
					plotChunk.initialize();
				}
				if (! plotChunk.getBlockList().isEmpty() && ! (plotChunk.getBlockList() == null)) {
					TownsRegenAPI.addPlotChunkSnapshot(plotChunk); // Save a snapshot.
				}

				plotChunk = null;
			}
		}
	}

	private void townUnclaim(Town town, WorldCoord worldCoord, boolean force) throws TownsException {
		try {
			TownBlock townBlock = worldCoord.getTownBlock();
			if (town != townBlock.getTown() && ! force) {
				throw new TownsException(TownsSettings.getLangString("msg_area_not_own"));
			}

			plugin.getTownsUniverse().removeTownBlock(townBlock);
			TownsUniverse.getDataSource().deleteTownBlock(townBlock);

			townBlock = null;
		} catch (NotRegisteredException e) {
			throw new TownsException(TownsSettings.getLangString("msg_not_claimed_1"));
		}
	}

	private void townUnclaimAll(Town town) {
		plugin.getTownsUniverse().removeTownBlocks(town);
		TownsMessaging.sendTownMessage(town, TownsSettings.getLangString("msg_abandoned_area_1"));
	}
}
