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
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockType;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;

/**
 * @author ElgarL
 */
public class PlotClaim extends Thread {
	Towns plugin;
	volatile Player player;
	volatile Resident resident;
	volatile TownsWorld world;
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
	public PlotClaim(Towns plugin, Player player, Resident resident, List<WorldCoord> selection, boolean claim) {
		super();
		this.plugin = plugin;
		this.player = player;
		this.resident = resident;
		this.selection = selection;
		this.claim = claim;
		this.setPriority(MIN_PRIORITY);
	}

	@Override
	public void run() {
		if (player != null) {
			TownsMessaging.sendMsg(player, "Processing " + ((claim) ? "Plot Claim..." : "Plot unclaim..."));
		}

		if (selection != null) {
			for (WorldCoord worldCoord : selection) {
				// Make sure this is a valid world (mainly when unclaiming).
				try {
					this.world = TownsUniverse.getWorld(worldCoord.getWorld().getName());
				} catch (NotRegisteredException e) {
					TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_err_not_configured"));
					continue;
				}
				try {
					if (claim) {
						residentClaim(worldCoord);
					} else {
						residentUnclaim(worldCoord);
					}
				}  catch (TownsException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			}
		} else if (! claim) {
			residentUnclaimAll();
		}

		if (player != null) {
			if (claim) {
				TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_claimed") + ((selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
			} else if (selection != null) {
				TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_unclaimed") + ((selection.size() > 5) ? "Total TownBlocks: " + selection.size() : Arrays.toString(selection.toArray(new WorldCoord[0]))));
			} else {
				TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_unclaimed"));
			}
		}

		TownsUniverse.getDataSource().saveResident(resident);
		plugin.updateCache();
	}

	private boolean residentClaim(WorldCoord worldCoord) throws TownsException {
		if (resident.hasTown()) {
			try {
				TownBlock townBlock = worldCoord.getTownBlock();
				Town town = townBlock.getTown();
				if (resident.getTown() != town && ! townBlock.getType().equals(TownBlockType.EMBASSY)) {
					throw new TownsException(TownsSettings.getLangString("msg_err_not_part_town"));
				}

				try {
					Resident owner = townBlock.getResident();
					if (townBlock.getPlotPrice() != - 1) {
						// Plot is for sale

						if (TownsSettings.isUsingEconomy() && ! resident.payTo(townBlock.getPlotPrice(), owner, "Plot - Buy From Seller")) {
							throw new TownsException(TownsSettings.getLangString("msg_no_money_purchase_plot"));
						}

						int maxPlots = TownsSettings.getMaxResidentPlots(resident);
						if (maxPlots >= 0 && resident.getTownBlocks().size() + 1 > maxPlots) {
							throw new TownsException(String.format(TownsSettings.getLangString("msg_max_plot_own"), maxPlots));
						}

						TownsMessaging.sendTownMessage(town, TownsSettings.getBuyResidentPlotMsg(resident.getName(), owner.getName(), townBlock.getPlotPrice()));
						townBlock.setPlotPrice(- 1);
						townBlock.setResident(resident);

						// Set the plot permissions to mirror the new owners.
						townBlock.setType(townBlock.getType());

						TownsUniverse.getDataSource().saveResident(owner);

						plugin.updateCache();
						return true;
					} else if (town.isMayor(resident) || town.hasAssistant(resident)) {
						//Plot isn't for sale but re-possessing for town.

						if (TownsSettings.isUsingEconomy() && ! town.payTo(townBlock.getPlotPrice(), owner, "Plot - Buy Back")) {
							throw new TownsException(TownsSettings.getLangString("msg_town_no_money_purchase_plot"));
						}

						TownsMessaging.sendTownMessage(town, TownsSettings.getBuyResidentPlotMsg(town.getName(), owner.getName(), townBlock.getPlotPrice()));
						townBlock.setResident(null);
						townBlock.setPlotPrice(- 1);

						// Set the plot permissions to mirror the towns.
						townBlock.setType(townBlock.getType());

						TownsUniverse.getDataSource().saveResident(owner);

						return true;
					} else {
						//Should never reach here.
						throw new AlreadyRegisteredException(String.format(TownsSettings.getLangString("msg_already_claimed"), owner.getName()));
					}
				} catch (NotRegisteredException e) {
					//Plot has no owner so it's the town selling it

					if (townBlock.getPlotPrice() == - 1) {
						throw new TownsException(TownsSettings.getLangString("msg_err_plot_nfs"));
					}

					if (TownsSettings.isUsingEconomy() && ! resident.payTo(townBlock.getPlotPrice(), town, "Plot - Buy From Town")) {
						throw new TownsException(TownsSettings.getLangString("msg_no_money_purchase_plot"));
					}

					townBlock.setPlotPrice(- 1);
					townBlock.setResident(resident);

					// Set the plot permissions to mirror the new owners.
					townBlock.setType(townBlock.getType());

					return true;
				}
			} catch (NotRegisteredException e) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_part_town"));
			}
		} else {
			throw new TownsException(TownsSettings.getLangString("msg_err_not_in_town_claim"));
		}
	}

	private boolean residentUnclaim(WorldCoord worldCoord) throws TownsException {
		try {
			TownBlock townBlock = worldCoord.getTownBlock();

			townBlock.setResident(null);
			townBlock.setPlotPrice(townBlock.getTown().getPlotPrice());

			// Set the plot permissions to mirror the towns.
			townBlock.setType(townBlock.getType());

			plugin.updateCache();
		} catch (NotRegisteredException e) {
			throw new TownsException(TownsSettings.getLangString("msg_not_own_place"));
		}

		return true;
	}

	private void residentUnclaimAll() {
		List<TownBlock> selection = new ArrayList<TownBlock>(resident.getTownBlocks());

		for (TownBlock townBlock : selection) {
			try {
				residentUnclaim(townBlock.getWorldCoord());
			} catch (TownsException e) {
				TownsMessaging.sendErrorMsg(player, e.getError());
			}
		}
	}
}
