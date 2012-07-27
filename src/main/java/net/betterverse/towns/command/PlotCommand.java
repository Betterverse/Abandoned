package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.TownsUtil;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockOwner;
import net.betterverse.towns.object.TownBlockType;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.PlotClaim;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.StringMgmt;

/**
 * Send a list of all general towns plot help commands to player Command: /plot
 */
public class PlotCommand implements CommandExecutor {
	private static Towns plugin;
	public static final List<String> output = new ArrayList<String>();
	static {
		output.add(ChatTools.formatTitle("/plot"));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing"), "/plot claim", "", TownsSettings.getLangString("msg_block_claim")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing"), "/plot claim", "[rect/circle] [radius]", ""));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot notforsale", "", TownsSettings.getLangString("msg_plot_nfs")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot notforsale", "[rect/circle] [radius]", ""));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot forsale [$]", "", TownsSettings.getLangString("msg_plot_fs")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot forsale [$]", "within [rect/circle] [radius]", ""));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot clear", "", ""));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing") + "/" + TownsSettings.getLangString("mayor_sing"), "/plot set ...", "", TownsSettings.getLangString("msg_plot_fs")));
		output.add(TownsSettings.getLangString("msg_nfs_abr"));
	}

	public PlotCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));
			if (args == null) {
				for (String line : output) {
					player.sendMessage(line);
				}
			} else {
				try {
					parsePlotCommand(player, args);
				} catch (TownsException x) {
					// No permisisons
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			}
		} else
		// Console
		{
			for (String line : output) {
				sender.sendMessage(Colors.strip(line));
			}
		}
		return true;
	}

	public void parsePlotCommand(Player player, String[] split) throws TownsException {
		if ((! plugin.isTownsAdmin(player)) && ((plugin.isPermissions()) && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.plot")))) {
			throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
		}

		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			for (String line : output) {
				player.sendMessage(line);
			}
		} else {
			Resident resident;
			TownsWorld world;
			Town town;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				world = TownsUniverse.getWorld(player.getWorld().getName());
				town = resident.getTown();
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			try {
				if (split[0].equalsIgnoreCase("claim")) {
					if (plugin.getTownsUniverse().isWarTime()) {
						throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
					}

					List<WorldCoord> selection = TownsUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord(player)), StringMgmt.remFirstArg(split));
					selection = TownsUtil.filterUnownedPlots(selection);
					int maxPlots = TownsSettings.getMaxResidentPlots(resident);

					if (maxPlots >= 0 && resident.getTownBlocks().size() + selection.size() > maxPlots) {
						throw new TownsException(String.format(TownsSettings.getLangString("msg_max_plot_own"), maxPlots));
					}

					if (selection.size() > 0) {
						double cost = 0;

						// Remove any plots Not for sale (if not the mayor) and tally up costs.
						for (WorldCoord worldCoord : new ArrayList<WorldCoord>(selection)) {
							double price = worldCoord.getTownBlock().getPlotPrice();
							if (price != - 1) {
								cost += worldCoord.getTownBlock().getPlotPrice();
							} else {
								if (worldCoord.getTownBlock().getTown().isMayor(resident) || worldCoord.getTownBlock().getTown().hasAssistant(resident)) {
									selection.remove(worldCoord);
								}
							}
						}

						if (TownsSettings.isUsingEconomy() && (! resident.canPayFromHoldings(cost))) {
							throw new TownsException(String.format(TownsSettings.getLangString("msg_no_funds_claim"), selection.size(), TownsEconomyObject.getFormattedBalance(cost)));
						}

						// Start the claim task
						new PlotClaim(plugin, player, resident, selection, true).start();
					} else {
						player.sendMessage(TownsSettings.getLangString("msg_err_empty_area_selection"));
					}
				} else if (split[0].equalsIgnoreCase("unclaim")) {
					if (plugin.getTownsUniverse().isWarTime()) {
						throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
					}

					if (split.length == 2 && split[1].equalsIgnoreCase("all"))
					// Start the unclaim task
					{
						new PlotClaim(plugin, player, resident, null, false).start();
					} else {
						List<WorldCoord> selection = TownsUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord(player)), StringMgmt.remFirstArg(split));
						selection = TownsUtil.filterOwnedBlocks(resident, selection);

						if (selection.size() > 0) {
							// Start the unclaim task
							new PlotClaim(plugin, player, resident, selection, false).start();
						} else {
							player.sendMessage(TownsSettings.getLangString("msg_err_empty_area_selection"));
						}
					}
				} else if (split[0].equalsIgnoreCase("notforsale") || split[0].equalsIgnoreCase("nfs")) {
					List<WorldCoord> selection = TownsUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord(player)), StringMgmt.remFirstArg(split));
					selection = TownsUtil.filterOwnedBlocks(resident.getTown(), selection);

					for (WorldCoord worldCoord : selection) {
						setPlotForSale(resident, worldCoord, - 1);
					}
				} else if (split[0].equalsIgnoreCase("forsale") || split[0].equalsIgnoreCase("fs")) {
					WorldCoord pos = new WorldCoord(world, Coord.parseCoord(player));
					double plotPrice = 0;
					switch (pos.getTownBlock().getType().ordinal()) {
						case 0:
							plotPrice = pos.getTownBlock().getTown().getPlotPrice();
							break;
						case 1:
							plotPrice = pos.getTownBlock().getTown().getCommercialPlotPrice();
							break;
					}

					if (split.length > 1) {
						int areaSelectPivot = TownsUtil.getAreaSelectPivot(split);
						List<WorldCoord> selection;
						if (areaSelectPivot >= 0) {
							selection = TownsUtil.selectWorldCoordArea(resident, new WorldCoord(world, Coord.parseCoord(player)), StringMgmt.subArray(split, areaSelectPivot + 1, split.length));
							selection = TownsUtil.filterOwnedBlocks(resident.getTown(), selection);
							if (selection.size() == 0) {
								player.sendMessage(TownsSettings.getLangString("msg_err_empty_area_selection"));
								return;
							}
						} else {
							selection = new ArrayList<WorldCoord>();
							selection.add(pos);
						}

						// Check that it's not: /plot forsale within rect 3
						if (areaSelectPivot != 1) {
							try {
								plotPrice = Double.parseDouble(split[1]);
							} catch (NumberFormatException e) {
								player.sendMessage(String.format(TownsSettings.getLangString("msg_error_must_be_num")));
								return;
							}
						}

						for (WorldCoord worldCoord : selection) {
							setPlotForSale(resident, worldCoord, plotPrice);
						}
					} else {
						setPlotForSale(resident, pos, plotPrice);
					}
				} else if (split[0].equalsIgnoreCase("perm")) {
					TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(player)).getTownBlock();
					TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(townBlock));
				} else if (split[0].equalsIgnoreCase("toggle")) {
					TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(player)).getTownBlock();
					// Test we are allowed to work on this plot
					plotTestOwner(resident, townBlock); //ignore the return as we are only checking for an exception
					town = townBlock.getTown();

					plotToggle(player, new WorldCoord(world, Coord.parseCoord(player)).getTownBlock(), StringMgmt.remFirstArg(split));
				} else if (split[0].equalsIgnoreCase("set")) {
					split = StringMgmt.remFirstArg(split);

					if (split.length > 0) {
						if (split[0].equalsIgnoreCase("perm")) {
							//Set plot level permissions (if the plot owner) or Mayor/Assistant of the town.

							TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(player)).getTownBlock();
							// Test we are allowed to work on this plot
							TownBlockOwner owner = plotTestOwner(resident, townBlock);
							town = townBlock.getTown();

							// Check we are allowed to set these perms
							toggleTest(player, townBlock, StringMgmt.join(StringMgmt.remFirstArg(split), ""));

							TownCommand.setTownBlockPermissions(player, owner, townBlock.getPermissions(), StringMgmt.remFirstArg(split), true);
							TownsUniverse.getDataSource().saveTownBlock(townBlock);
							return;
						}

						WorldCoord worldCoord = new WorldCoord(world, Coord.parseCoord(player));
						setPlotType(resident, worldCoord, split[0]);
						TownsUniverse.getDataSource().saveTownBlock(worldCoord.getTownBlock());
						player.sendMessage(String.format(TownsSettings.getLangString("msg_plot_set_type"), split[0]));
					} else {
						player.sendMessage(ChatTools.formatCommand("", "/plot set", "reset", ""));
						player.sendMessage(ChatTools.formatCommand("", "/plot set", "shop|embassy|arena|wilds", ""));
						player.sendMessage(ChatTools.formatCommand("", "/plot set perm", "?", ""));
					}
				} else if (split[0].equalsIgnoreCase("clear")) {
					if (! town.isMayor(resident)) {
						player.sendMessage(TownsSettings.getLangString("msg_not_mayor"));
						return;
					}

					TownBlock townBlock = new WorldCoord(world, Coord.parseCoord(player)).getTownBlock();

					if (townBlock != null) {
						if (townBlock.isOwner(town) && (! townBlock.hasResident())) {
							for (String material : world.getPlotManagementMayorDelete()) {
								if (Material.matchMaterial(material) != null) {
									plugin.getTownsUniverse().deleteTownBlockMaterial(townBlock, Material.getMaterial(material).getId());
									player.sendMessage(String.format(TownsSettings.getLangString("msg_clear_plot_material"), material));
								} else {
									throw new TownsException(String.format(TownsSettings.getLangString("msg_err_invalid_property"), material));
								}
							}
						} else {
							throw new TownsException(String.format(TownsSettings.getLangString("msg_already_claimed"), townBlock.getResident().getName()));
						}
					} else {
						// Shouldn't ever reach here as a null townBlock should be caught already in WorldCoord.
						player.sendMessage(TownsSettings.getLangString("msg_err_empty_area_selection"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
			} 
		}
	}

	/**
	 * Set the plot type if we are permitted
	 *
	 * @param resident
	 * @param worldCoord
	 * @param type
	 * @throws TownsException
	 */
	public void setPlotType(Resident resident, WorldCoord worldCoord, String type) throws TownsException {
		if (resident.hasTown()) {
			try {
				TownBlock townBlock = worldCoord.getTownBlock();

				// Test we are allowed to work on this plot
				plotTestOwner(resident, townBlock); //ignore the return as we are only checking for an exception

				townBlock.setType(type);
			} catch (NotRegisteredException e) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_part_town"));
			}
		} else {
			throw new TownsException(TownsSettings.getLangString("msg_err_must_belong_town"));
		}
	}

	/**
	 * Set the plot for sale/not for sale
	 * if permitted
	 *
	 * @param resident
	 * @param worldCoord
	 * @param forSale
	 * @throws TownsException
	 */
	public void setPlotForSale(Resident resident, WorldCoord worldCoord, double forSale) throws TownsException {
		if (resident.hasTown()) {
			try {
				TownBlock townBlock = worldCoord.getTownBlock();

				// Test we are allowed to work on this plot
				plotTestOwner(resident, townBlock); //ignore the return as we are only checking for an exception

				townBlock.setPlotPrice(forSale);

				if (forSale != - 1) {
					TownsMessaging.sendTownMessage(townBlock.getTown(), TownsSettings.getPlotForSaleMsg(resident.getName(), worldCoord));
				} else {
					plugin.getTownsUniverse().getPlayer(resident).sendMessage(TownsSettings.getLangString("msg_err_plot_nfs"));
				}
			} catch (NotRegisteredException e) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_part_town"));
			}
		} else {
			throw new TownsException(TownsSettings.getLangString("msg_err_must_belong_town"));
		}
	}

	/**
	 * Toggle the plots flags for pvp/explosion/fire/mobs
	 * (if town/world permissions allow)
	 *
	 * @param player
	 * @param townBlock
	 * @param split
	 */
	public void plotToggle(Player player, TownBlock townBlock, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/res toggle"));
			player.sendMessage(ChatTools.formatCommand("", "/res toggle", "pvp", ""));
			player.sendMessage(ChatTools.formatCommand("", "/res toggle", "explosion", ""));
			player.sendMessage(ChatTools.formatCommand("", "/res toggle", "fire", ""));
			player.sendMessage(ChatTools.formatCommand("", "/res toggle", "mobs", ""));
		} else {
			try {
				// TODO: Let admin's call a subfunction of this.
				if (split[0].equalsIgnoreCase("pvp")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, townBlock, StringMgmt.join(StringMgmt.remFirstArg(split), " "));
					townBlock.getPermissions().pvp = ! townBlock.getPermissions().pvp;
					TownsMessaging.sendMessage(player, String.format(TownsSettings.getLangString("msg_changed_pvp"), "Plot", townBlock.getPermissions().pvp ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("explosion")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, townBlock, StringMgmt.join(StringMgmt.remFirstArg(split), " "));
					townBlock.getPermissions().explosion = ! townBlock.getPermissions().explosion;
					TownsMessaging.sendMessage(player, String.format(TownsSettings.getLangString("msg_changed_expl"), "the Plot", townBlock.getPermissions().explosion ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("fire")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, townBlock, StringMgmt.join(StringMgmt.remFirstArg(split), " "));
					townBlock.getPermissions().fire = ! townBlock.getPermissions().fire;
					TownsMessaging.sendMessage(player, String.format(TownsSettings.getLangString("msg_changed_fire"), "the Plot", townBlock.getPermissions().fire ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("mobs")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, townBlock, StringMgmt.join(StringMgmt.remFirstArg(split), " "));
					townBlock.getPermissions().mobs = ! townBlock.getPermissions().mobs;
					TownsMessaging.sendMessage(player, String.format(TownsSettings.getLangString("msg_changed_mobs"), "the Plot", townBlock.getPermissions().mobs ? "Enabled" : "Disabled"));
				} else {
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "plot"));
					return;
				}
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(player, e.getMessage());
			}

			TownsUniverse.getDataSource().saveTownBlock(townBlock);
		}
	}

	/**
	 * Check the world and town settings to see
	 * if we are allowed to alter these settings
	 *
	 * @param player
	 * @param townBlock
	 * @param split
	 * @throws TownsException if toggle is not permitted
	 */
	private void toggleTest(Player player, TownBlock townBlock, String split) throws TownsException {
		//Make sure we are allowed to set these permissions.
		Town town = townBlock.getTown();

		if (split.contains("mobs")) {
			if (town.getWorld().isForceTownMobs()) {
				throw new TownsException(TownsSettings.getLangString("msg_world_mobs"));
			}
			if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.toggle.mobs"))) {
				throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
			}
		}

		if (split.contains("fire")) {
			if (town.getWorld().isForceFire()) {
				throw new TownsException(TownsSettings.getLangString("msg_world_fire"));
			}
			if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.toggle.fire"))) {
				throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
			}
		}

		if (split.contains("explosion")) {
			if (town.getWorld().isForceExpl()) {
				throw new TownsException(TownsSettings.getLangString("msg_world_expl"));
			}
			if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.toggle.explosions"))) {
				throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
			}
		}

		if (split.contains("pvp")) {
			if (town.getWorld().isForcePVP()) {
				throw new TownsException(TownsSettings.getLangString("msg_world_pvp"));
			}
			if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.toggle.pvp"))) {
				throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
			}
		}
		if ((split.contains("pvp")) || (split.trim().equalsIgnoreCase("off"))) {
			if (townBlock.getType().equals(TownBlockType.ARENA)) {
				throw new TownsException(TownsSettings.getLangString("msg_plot_pvp"));
			}
		}
	}

	/**
	 * Test the townBlock to ensure we are either the plot owner, or the
	 * mayor/assistant
	 *
	 * @param resident
	 * @param townBlock
	 * @throws TownsException
	 */
	public TownBlockOwner plotTestOwner(Resident resident, TownBlock townBlock) throws TownsException {
		if (townBlock.hasResident()) {
			Resident owner = townBlock.getResident();

			if (resident != owner) {
				throw new TownsException(TownsSettings.getLangString("msg_area_not_own"));
			}

			return owner;
		} else {
			Town owner = townBlock.getTown();

			if ((! resident.isMayor()) && (! owner.hasAssistant(resident))) {
				throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
			}

			if ((resident.getTown() != owner)) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_part_town"));
			}

			return owner;
		}
	}
}
