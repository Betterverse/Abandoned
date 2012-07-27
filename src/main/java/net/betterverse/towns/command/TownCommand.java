package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.naming.InvalidNameException;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.questioner.Questioner;
import net.betterverse.questioner.questionmanager.Option;
import net.betterverse.questioner.questionmanager.Question;
import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyTownException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.TownsUtil;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.PlotBlockData;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockOwner;
import net.betterverse.towns.object.TownSpawnLevel;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsPermission;
import net.betterverse.towns.object.TownsRegenAPI;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.questioner.JoinTownTask;
import net.betterverse.towns.questioner.ResidentTownQuestionTask;
import net.betterverse.towns.tasks.TownClaim;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.StringMgmt;

/**
 * Send a list of all town help commands to player
 * Command: /town
 */

public class TownCommand implements CommandExecutor {
	private static Towns plugin;
	private static final List<String> output = new ArrayList<String>();
	static {
		output.add(ChatTools.formatTitle("/town"));
		output.add(ChatTools.formatCommand("", "/town", "", TownsSettings.getLangString("town_help_1")));
		output.add(ChatTools.formatCommand("", "/town", "[town]", TownsSettings.getLangString("town_help_3")));
		output.add(ChatTools.formatCommand("", "/town", "here", TownsSettings.getLangString("town_help_4")));
		output.add(ChatTools.formatCommand("", "/town", "list", ""));
		output.add(ChatTools.formatCommand("", "/town", "online", TownsSettings.getLangString("town_help_10")));
		output.add(ChatTools.formatCommand("", "/town", "leave", ""));
		output.add(ChatTools.formatCommand("", "/town", "spawn", TownsSettings.getLangString("town_help_5")));
		if (! TownsSettings.isTownCreationAdminOnly()) {
			output.add(ChatTools.formatCommand("", "/town", "new [town]", TownsSettings.getLangString("town_help_6")));
		}
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/town", "new [town] " + TownsSettings.getLangString("town_help_2"), TownsSettings.getLangString("town_help_7")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing"), "/town", "deposit [$]", ""));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "mayor ?", TownsSettings.getLangString("town_help_8")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/town", "delete [town]", ""));
	}

	public TownCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));
			parseTownCommand(player, args);
		} else
		// Console
		{
			for (String line : output) {
				sender.sendMessage(Colors.strip(line));
			}
		}
		return true;
	}

	private void parseTownCommand(Player player, String[] split) {
		if (split.length == 0) {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(player.getName());
				Town town = resident.getTown();
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(town));
			} catch (NotRegisteredException x) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_dont_belong_town"));
			}
		} else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
			for (String line : output) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("here")) {
			showTownStatusHere(player);
		} else if (split[0].equalsIgnoreCase("list")) {
			listTowns(player);
		} else if (split[0].equalsIgnoreCase("new")) {
			if (split.length == 1) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_specify_name"));
			} else if (split.length == 2) {
				newTown(player, split[1], player.getName());
			} else
			// TODO: Check if player is an admin
			{
				newTown(player, split[1], split[2]);
			}
		} else if (split[0].equalsIgnoreCase("leave")) {
			townLeave(player);
		} else if (split[0].equalsIgnoreCase("withdraw")) {
			if (split.length == 2) {
				try {
					townWithdraw(player, Integer.parseInt(split[1].trim()));
				} catch (NumberFormatException e) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_int"));
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_must_specify_amnt"), "/town withdraw"));
			}
		} else if (split[0].equalsIgnoreCase("deposit")) {
			if (split.length == 2) {
				try {
					townDeposit(player, Integer.parseInt(split[1].trim()));
				} catch (NumberFormatException e) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_int"));
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_must_specify_amnt"), "/town deposit"));
			}
		} else {
			String[] newSplit = StringMgmt.remFirstArg(split);

			if (split[0].equalsIgnoreCase("set")) {
				townSet(player, newSplit);
			} else if (split[0].equalsIgnoreCase("buy")) {
				townBuy(player, newSplit);
			} else if (split[0].equalsIgnoreCase("toggle")) {
				townToggle(player, newSplit);
			} else if (split[0].equalsIgnoreCase("mayor")) {
				townMayor(player, newSplit);
			} else if (split[0].equalsIgnoreCase("assistant")) {
				townAssistant(player, newSplit);
			} else if (split[0].equalsIgnoreCase("spawn")) {
				townSpawn(player, newSplit);
			} else if (split[0].equalsIgnoreCase("delete")) {
				townDelete(player, newSplit);
			} else if (split[0].equalsIgnoreCase("add")) {
				townAdd(player, null, newSplit);
			} else if (split[0].equalsIgnoreCase("kick")) {
				townKick(player, newSplit);
			} else if (split[0].equalsIgnoreCase("claim")) {
				parseTownClaimCommand(player, newSplit);
			} else if (split[0].equalsIgnoreCase("unclaim")) {
				parseTownUnclaimCommand(player, newSplit);
			} else if (split[0].equalsIgnoreCase("online")) {
				try {
					Resident resident = plugin.getTownsUniverse().getResident(player.getName());
					Town town = resident.getTown();
					TownsMessaging.sendMessage(player, TownsFormatter.getFormattedOnlineResidents(plugin, TownsSettings.getLangString("msg_town_online"), town));
				} catch (NotRegisteredException x) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_dont_belong_town"));
				}
			} else {
				try {
					Town town = plugin.getTownsUniverse().getTown(split[0]);
					TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(town));
				} catch (NotRegisteredException x) {
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_registered_1"), split[0]));
				}
			}
		}
	}

	/**
	 * Send a list of all towns in the universe to player Command: /town list
	 *
	 * @param player
	 */

	public void listTowns(Player player) {
		player.sendMessage(ChatTools.formatTitle(TownsSettings.getLangString("town_plu")));
		ArrayList<String> formatedList = new ArrayList<String>();
		for (Town town : plugin.getTownsUniverse().getTowns()) {
			formatedList.add(Colors.LightBlue + town.getName() + Colors.Blue + " [" + town.getNumResidents() + "]" + Colors.White);
		}
		for (String line : ChatTools.list(formatedList)) {
			player.sendMessage(line);
		}
	}

	public void townMayor(Player player, String[] split) {
		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			showTownMayorHelp(player);
		}
	}

	public void townAssistant(Player player, String[] split) {
		if (split.length == 0) {
			//TODO: assistant help
		} else if (split[0].equalsIgnoreCase("add")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			townAssistantsAdd(player, newSplit);
		} else if (split[0].equalsIgnoreCase("remove")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			townAssistantsRemove(player, newSplit);
		}
	}

	/**
	 * Send a the status of the town the player is physically at to him
	 *
	 * @param player
	 */
	public void showTownStatusHere(Player player) {
		try {
			TownsWorld world = TownsUniverse.getWorld(player.getWorld().getName());
			Coord coord = Coord.parseCoord(player);
			showTownStatusAtCoord(player, world, coord);
		} catch (TownsException e) {
			TownsMessaging.sendErrorMsg(player, e.getError());
		}
	}

	/**
	 * Send a the status of the town at the target coordinates to the player
	 *
	 * @param player
	 * @param world
	 * @param coord
	 * @throws TownsException
	 */
	public void showTownStatusAtCoord(Player player, TownsWorld world, Coord coord) throws TownsException {
		if (! world.hasTownBlock(coord)) {
			throw new TownsException(String.format(TownsSettings.getLangString("msg_not_claimed"), coord));
		}

		Town town = world.getTownBlock(coord).getTown();
		TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(town));
	}

	public void showTownMayorHelp(Player player) {
		player.sendMessage(ChatTools.formatTitle("Town Mayor Help"));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "withdraw [$]", ""));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "claim", "'/town claim ?' " + TownsSettings.getLangString("res_5")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "unclaim", "'/town " + TownsSettings.getLangString("res_5")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "[add/kick] " + TownsSettings.getLangString("res_2") + " .. []", TownsSettings.getLangString("res_6")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "[add+/kick+] " + TownsSettings.getLangString("res_2"), TownsSettings.getLangString("res_7")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "set [] .. []", "'/town set' " + TownsSettings.getLangString("res_5")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "buy [] .. []", "'/town buy' " + TownsSettings.getLangString("res_5")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "toggle", ""));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "assistant [add/remove] [player]", TownsSettings.getLangString("res_6")));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "assistant [add+/remove+] [player]", TownsSettings.getLangString("res_7")));
		// TODO: player.sendMessage(ChatTools.formatCommand("Mayor", "/town", "wall [type] [height]", ""));
		// TODO: player.sendMessage(ChatTools.formatCommand("Mayor", "/town", "wall remove", ""));
		player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town", "delete", ""));
	}

	public void townToggle(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/town toggle"));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "pvp", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "public", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "explosion", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "fire", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "mobs", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town toggle", "taxpercent", ""));
		} else {
			Resident resident;
			Town town;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				town = resident.getTown();
				if (! resident.isMayor()) {
					if (! town.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			try {
				// TODO: Let admin's call a subfunction of this.
				if (split[0].equalsIgnoreCase("pvp")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, town, StringMgmt.join(split, " "));
					town.setPVP(! town.isPVP());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_pvp"), "Town", town.isPVP() ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("public")) {
					if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.toggle.public"))) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
						return;
					}
					town.setPublic(! town.isPublic());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_public"), town.isPublic() ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("explosion")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, town, StringMgmt.join(split, " "));
					town.setBANG(! town.isBANG());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_expl"), "Town", town.isBANG() ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("fire")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, town, StringMgmt.join(split, " "));
					town.setFire(! town.isFire());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_fire"), "Town", town.isFire() ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("mobs")) {
					//Make sure we are allowed to set these permissions.
					toggleTest(player, town, StringMgmt.join(split, " "));
					town.setHasMobs(! town.hasMobs());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_mobs"), "Town", town.hasMobs() ? "Enabled" : "Disabled"));
				} else if (split[0].equalsIgnoreCase("taxpercent")) {
					town.setTaxPercentage(! town.isTaxPercentage());
					TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_changed_taxpercent"), town.isTaxPercentage() ? "Enabled" : "Disabled"));
				} else {
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "town"));
					return;
				}
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(player, e.getMessage());
			}

			TownsUniverse.getDataSource().saveTown(town);
		}
	}

	private void toggleTest(Player player, Town town, String split) throws TownsException {
		//Make sure we are allowed to set these permissions.

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
	}

	public void townSet(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/town set"));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "board [message ... ]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "mayor " + TownsSettings.getLangString("town_help_2"), ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "homeblock", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "spawn", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "perm ...", "'/town set perm' " + TownsSettings.getLangString("res_5")));
			//player.sendMessage(ChatTools.formatCommand("", "/town set", "pvp [on/off]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "taxes [$]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "[plottax/shoptax/embassytax] [$]", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/town set", "shoptax [$]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "[plotprice/shopprice/embassyprice] [$]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "name [name]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/town set", "tag [upto 4 letters] or clear", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/town set", "public [on/off]", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/town set", "explosion [on/off]", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/town set", "fire [on/off]", ""));
		} else {
			Resident resident;
			Town town;
			TownsWorld oldWorld = null;

			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				town = resident.getTown();
				if (! resident.isMayor()) {
					if (! town.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			// TODO: Let admin's call a subfunction of this.
			if (split[0].equalsIgnoreCase("board")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set board " + TownsSettings.getLangString("town_help_9"));
					return;
				} else {
					String line = split[1];
					for (int i = 2; i < split.length; i++) {
						line += " " + split[i];
					}
					town.setTownBoard(line);
					TownsMessaging.sendTownBoard(player, town);
				}
			} else if (split[0].equalsIgnoreCase("mayor")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set mayor Dumbo");
					return;
				} else {
					try {
						if (! resident.isMayor()) {
							throw new TownsException(TownsSettings.getLangString("msg_not_mayor"));
						}

						String oldMayor = town.getMayor().getName();
						Resident newMayor = plugin.getTownsUniverse().getResident(split[1]);
						town.setMayor(newMayor);
						plugin.deleteCache(oldMayor);
						plugin.deleteCache(newMayor.getName());
						TownsMessaging.sendTownMessage(town, TownsSettings.getNewMayorMsg(newMayor.getName()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getError());
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("taxes")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set taxes 7");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						if (town.isTaxPercentage() && amount > 100) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_percentage"));
							return;
						}
						town.setTaxes(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_tax"), player.getName(), split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("plottax")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set plottax 10");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setPlotTax(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_plottax"), player.getName(), split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("shoptax")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set shoptax 10");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setCommercialPlotTax(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_alttax"), player.getName(), "shop", split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("embassytax")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set embassytax 10");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setEmbassyPlotTax(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_alttax"), player.getName(), "embassy", split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("plotprice")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set plotprice 50");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setPlotPrice(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_plotprice"), player.getName(), split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("shopprice")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set shopprice 50");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setCommercialPlotPrice(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_altprice"), player.getName(), "shop", split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("embassyprice")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set embassyprice 50");
					return;
				} else {
					try {
						Double amount = Double.parseDouble(split[1]);
						if (amount < 0) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
							return;
						}
						town.setEmbassyPlotPrice(amount);
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_altprice"), player.getName(), "embassy", split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_num"));
						return;
					}
				}
			} else if (split[0].equalsIgnoreCase("name")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set name BillyBobTown");
					return;
				} else if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.rename"))) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
					return;
				}

				//TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_town_rename_disabled"));
				if (TownsSettings.isValidRegionName(split[1])) {
					townRename(player, town, split[1]);
				} else {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
				}
			} else if (split[0].equalsIgnoreCase("tag")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /town set tag PLTC");
				} else if (split[1].equalsIgnoreCase("clear")) {
					try {
						town.setTag(" ");
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_reset_town_tag"), player.getName()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					}
				} else {
					try {
						town.setTag(plugin.getTownsUniverse().checkAndFilterName(split[1]));
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_set_town_tag"), player.getName(), town.getTag()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					} catch (InvalidNameException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					}
				}
			} else if (split[0].equalsIgnoreCase("homeblock")) {
				Coord coord = Coord.parseCoord(player);
				TownBlock townBlock;
				TownsWorld world;
				try {
					if (plugin.getTownsUniverse().isWarTime()) {
						throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
					}

					world = TownsUniverse.getWorld(player.getWorld().getName());
					if (world.getMinDistanceFromOtherTowns(coord, resident.getTown()) < TownsSettings.getMinDistanceFromTownHomeblocks()) {
						throw new TownsException(TownsSettings.getLangString("msg_too_close"));
					}

					if (TownsSettings.getMaxDistanceBetweenHomeblocks() > 0) {
						if ((world.getMinDistanceFromOtherTowns(coord, resident.getTown()) > TownsSettings.getMaxDistanceBetweenHomeblocks()) && world.hasTowns()) {
							throw new TownsException(TownsSettings.getLangString("msg_too_far"));
						}
					}

					townBlock = TownsUniverse.getWorld(player.getWorld().getName()).getTownBlock(coord);
					oldWorld = town.getWorld();
					town.setHomeBlock(townBlock);
					TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_town_home"), coord.toString()));
				} catch (TownsException e) {
					TownsMessaging.sendErrorMsg(player, e.getError());
					return;
				}
			} else if (split[0].equalsIgnoreCase("spawn")) {
				try {
					town.setSpawn(player.getLocation());
					TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_set_town_spawn"));
				} catch (TownsException e) {
					TownsMessaging.sendErrorMsg(player, e.getError());
					return;
				}
			} else if (split[0].equalsIgnoreCase("perm")) {
				//Make sure we are allowed to set these permissions.
				if(!player.hasPermission("towns.set.perm")) {
					TownsMessaging.sendErrorMsg(player, "You are not allowed to do that!");
					return;
				}
				try {
					toggleTest(player, town, StringMgmt.join(split, " "));
				} catch (Exception e) {
					TownsMessaging.sendErrorMsg(player, e.getMessage());
					return;
				}
				String[] newSplit = StringMgmt.remFirstArg(split);
				setTownBlockOwnerPermissions(player, town, newSplit);
				plugin.updateCache();
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "town"));
				return;
			}

			TownsUniverse.getDataSource().saveTown(town);

			// If the town (homeblock) has moved worlds we need to update the world files.
			if (oldWorld != null) {
				TownsUniverse.getDataSource().saveWorld(town.getWorld());
				TownsUniverse.getDataSource().saveWorld(oldWorld);
			}
		}
	}

	public void townBuy(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/town buy"));
			if (TownsSettings.isSellingBonusBlocks()) {
				String line = Colors.Yellow + "[Purchased Bonus] " + Colors.Green + "Cost: " + Colors.LightGreen + "%s" + Colors.Gray + " | " + Colors.Green + "Max: " + Colors.LightGreen + "%d";
				player.sendMessage(String.format(line, TownsFormatter.formatMoney(TownsSettings.getPurchasedBonusBlocksCost()), TownsSettings.getMaxPurchedBlocks()));
				player.sendMessage(ChatTools.formatCommand("", "/town buy", "bonus [n]", ""));
			} else {
				// Temp placeholder.
				player.sendMessage("Nothing for sale right now.");
			}
		} else {
			Resident resident;
			Town town;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				town = resident.getTown();
				if (! resident.isMayor()) {
					if (! town.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
			try {
				if (split[0].equalsIgnoreCase("bonus")) {
					if (split.length == 2) {
						try {
							int bought = townBuyBonusTownBlocks(town, Integer.parseInt(split[1].trim()));
							double cost = bought * TownsSettings.getPurchasedBonusBlocksCost();
							TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_buy"), bought, "bonus town blocks", TownsFormatter.formatMoney(cost)));
						} catch (NumberFormatException e) {
							throw new TownsException(TownsSettings.getLangString("msg_error_must_be_int"));
						}
					} else {
						throw new TownsException(String.format(TownsSettings.getLangString("msg_must_specify_amnt"), "/town buy bonus"));
					}
				}

				TownsUniverse.getDataSource().saveTown(town);
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
			}
		}
	}

	/**
	 * Town buys bonus blocks after checking the configured maximum.
	 *
	 * @param town
	 * @param inputN
	 * @return The number of purchased bonus blocks.
	 * @throws TownsException
	 */
	public static int townBuyBonusTownBlocks(Town town, int inputN) throws TownsException {
		if (inputN < 0) {
			throw new TownsException(TownsSettings.getLangString("msg_err_negative"));
		}

		int current = town.getPurchasedBlocks();

		int n;
		if (current + inputN > TownsSettings.getMaxPurchedBlocks()) {
			n = TownsSettings.getMaxPurchedBlocks() - current;
		} else {
			n = inputN;
		}

		if (n == 0) {
			return n;
		}

			double cost = n * TownsSettings.getPurchasedBonusBlocksCost();
			if (TownsSettings.isUsingEconomy() && ! town.pay(cost, String.format("Town Buy Bonus (%d)", n))) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_no_funds_to_buy"), n, "bonus town blocks", cost + TownsEconomyObject.getEconomyCurrency()));
			}

		town.addPurchasedBlocks(n);

		return n;
	}

	/**
	 * Create a new town. Command: /town new [town] *[mayor]
	 *
	 * @param player
	 */

	public void newTown(Player player, String name, String mayorName) {
		TownsUniverse universe = plugin.getTownsUniverse();
		try {
			if (universe.isWarTime()) {
				throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
			}

			if (! plugin.isTownsAdmin(player) && (TownsSettings.isTownCreationAdminOnly() || (plugin.isPermissions() && ! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.new")))) {
				throw new TownsException(TownsSettings.getNotPermToNewTownLine());
			}

			if (TownsSettings.hasTownLimit() && universe.getTowns().size() >= TownsSettings.getTownLimit()) {
				throw new TownsException(TownsSettings.getLangString("msg_err_universe_limit"));
			}

			if (! TownsSettings.isValidRegionName(name)) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_err_invalid_name"), name));
			}

			Resident resident = universe.getResident(mayorName);
			if (resident.hasTown()) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_err_already_res"), resident.getName()));
			}

			TownsWorld world = TownsUniverse.getWorld(player.getWorld().getName());

			if (! world.isUsingTowns()) {
				throw new TownsException(TownsSettings.getLangString("msg_set_use_towns_off"));
			}

			Coord key = Coord.parseCoord(player);
			if (world.hasTownBlock(key)) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_already_claimed_1"), key));
			}

			if (world.getMinDistanceFromOtherTowns(key) < TownsSettings.getMinDistanceFromTownHomeblocks()) {
				throw new TownsException(TownsSettings.getLangString("msg_too_close"));
			}

			if (TownsSettings.getMaxDistanceBetweenHomeblocks() > 0) {
				if ((world.getMinDistanceFromOtherTowns(key) > TownsSettings.getMaxDistanceBetweenHomeblocks()) && world.hasTowns()) {
					throw new TownsException(TownsSettings.getLangString("msg_too_far"));
				}
			}

			if (TownsSettings.isUsingEconomy() && ! resident.pay(TownsSettings.getNewTownPrice(), "New Town Cost")) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_no_funds_new_town"), (resident.getName().equals(player.getName()) ? "You" : resident.getName())));
			}

			newTown(universe, world, name, resident, key, player.getLocation());
			TownsMessaging.sendGlobalMessage(TownsSettings.getNewTownMsg(player.getName(), name));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			// TODO: delete town data that might have been done
		} 
	}

	public Town newTown(TownsUniverse universe, TownsWorld world, String name, Resident resident, Coord key, Location spawn) throws TownsException {
		world.newTownBlock(key);
		universe.newTown(name);
		Town town = universe.getTown(name);
		town.addResident(resident);
		town.setMayor(resident);
		TownBlock townBlock = world.getTownBlock(key);
		townBlock.setTown(town);
		town.setHomeBlock(townBlock);
		town.setSpawn(spawn);
		//world.addTown(town);

		if (world.isUsingPlotManagementRevert()) {
			PlotBlockData plotChunk = TownsRegenAPI.getPlotChunk(townBlock);
			if (plotChunk != null) {
				TownsRegenAPI.deletePlotChunk(plotChunk); // just claimed so stop regeneration.
			} else {
				plotChunk = new PlotBlockData(townBlock); // Not regenerating so create a new snapshot.
				plotChunk.initialize();
			}
			TownsRegenAPI.addPlotChunkSnapshot(plotChunk); // Save a snapshot.
			plotChunk = null;
		}
		TownsMessaging.sendDebugMsg("Creating new Town account: " + "town-" + name);
		if (TownsSettings.isUsingEconomy()) {
			town.setBalance(0);
		}

		TownsUniverse.getDataSource().saveResident(resident);
		TownsUniverse.getDataSource().saveTown(town);
		TownsUniverse.getDataSource().saveWorld(world);
		TownsUniverse.getDataSource().saveTownList();

		plugin.updateCache();
		return town;
	}

	public void townRename(Player player, Town town, String newName) {
		try {
			plugin.getTownsUniverse().renameTown(town, newName);
			TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_town_set_name"), player.getName(), town.getName()));
		} catch (TownsException e) {
			TownsMessaging.sendErrorMsg(player, e.getError());
		}
	}

	public void townLeave(Player player) {
		Resident resident;
		Town town;
		try {
			//TODO: Allow leaving town during war.
			if (plugin.getTownsUniverse().isWarTime()) {
				throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
			}

			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();
			plugin.deleteCache(resident.getName());
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		if (resident.isMayor()) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getMayorAbondonMsg());
			return;
		}

		try {
			town.removeResident(resident);
		} catch (EmptyTownException et) {
			plugin.getTownsUniverse().removeTown(et.getTown());
		} catch (NotRegisteredException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		TownsUniverse.getDataSource().saveResident(resident);
		TownsUniverse.getDataSource().saveTown(town);

		plugin.updateCache();

		TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_left_town"), resident.getName()));
		TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_left_town"), resident.getName()));
	}

	public static void townSpawn(Player player, String[] split) {
		try {
			boolean isTownsAdmin = plugin.isTownsAdmin(player);
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());
			Town town;
			String notAffordMSG;
			TownSpawnLevel townSpawnPermission;

			// Set target town and affiliated messages.
			if (split.length == 0) {
				town = resident.getTown();
				notAffordMSG = TownsSettings.getLangString("msg_err_cant_afford_tp");
			} else {
				// split.length > 1
				town = plugin.getTownsUniverse().getTown(split[0]);
				notAffordMSG = String.format(TownsSettings.getLangString("msg_err_cant_afford_tp_town"), town.getName());
			}

			// Determine conditions
			if (isTownsAdmin) {
				townSpawnPermission = TownSpawnLevel.ADMIN;
			} else if (split.length == 0) {
				townSpawnPermission = TownSpawnLevel.TOWN_RESIDENT;
			} else {
				// split.length > 1
				if (! resident.hasTown()) {
					townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
				} else if (resident.getTown() == town) {
					townSpawnPermission = TownSpawnLevel.TOWN_RESIDENT;
				} else if (resident.hasNation() && town.hasNation()) {
					Nation playerNation = resident.getTown().getNation();
					Nation targetNation = town.getNation();

					if (playerNation == targetNation) {
						townSpawnPermission = TownSpawnLevel.PART_OF_NATION;
					} else if (targetNation.hasEnemy(playerNation)) {
						// Prevent enemies from using spawn travel.
						throw new TownsException(TownsSettings.getLangString("msg_err_public_spawn_enemy"));
					} else if (targetNation.hasAlly(playerNation)) {
						townSpawnPermission = TownSpawnLevel.NATION_ALLY;
					} else {
						townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
					}
				} else {
					townSpawnPermission = TownSpawnLevel.UNAFFILIATED;
				}
			}

			TownsMessaging.sendDebugMsg(townSpawnPermission.toString() + " " + townSpawnPermission.isAllowed());
			townSpawnPermission.checkIfAllowed(plugin, player);

			if (! (isTownsAdmin || townSpawnPermission == TownSpawnLevel.TOWN_RESIDENT) && ! town.isPublic()) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_public"));
			}

			if (! isTownsAdmin) {
				// Prevent spawn travel while in disallowed zones (if configured)
				List<String> disallowedZones = TownsSettings.getDisallowedTownSpawnZones();

				if (! disallowedZones.isEmpty()) {
					String inTown = null;
					try {
						Location loc = plugin.getCache(player).getLastLocation();
						inTown = plugin.getTownsUniverse().getTownName(loc);
					} catch (NullPointerException e) {
						inTown = plugin.getTownsUniverse().getTownName(player.getLocation());
					}

					if (inTown == null && disallowedZones.contains("unclaimed")) {
						throw new TownsException(String.format(TownsSettings.getLangString("msg_err_town_spawn_disallowed_from"), "the Wilderness"));
					}
					if (inTown != null && resident.hasNation() && plugin.getTownsUniverse().getTown(inTown).hasNation()) {
						Nation inNation = plugin.getTownsUniverse().getTown(inTown).getNation();
						Nation playerNation = resident.getTown().getNation();
						if (inNation.hasEnemy(playerNation) && disallowedZones.contains("enemy")) {
							throw new TownsException(String.format(TownsSettings.getLangString("msg_err_town_spawn_disallowed_from"), "Enemy areas"));
						}
						if (! inNation.hasAlly(playerNation) && ! inNation.hasEnemy(playerNation) && disallowedZones.contains("neutral")) {
							throw new TownsException(String.format(TownsSettings.getLangString("msg_err_town_spawn_disallowed_from"), "Neutral towns"));
						}
					}
				}
			}

			double travelCost = townSpawnPermission.getCost();

			// Check if need/can pay
			if (travelCost > 0 && TownsSettings.isUsingEconomy() && (resident.getHoldingBalance() < travelCost)) {
				throw new TownsException(notAffordMSG);
			}

			// Used later to make sure the chunk we teleport to is loaded.
			Chunk chunk = town.getSpawn().getWorld().getChunkAt(town.getSpawn().getBlock());

			// Show message if we are using iConomy and are charging for spawn travel.
			if (travelCost > 0 && TownsSettings.isUsingEconomy() && resident.payTo(travelCost, town, String.format("Town Spawn (%s)", townSpawnPermission))) {
				TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_cost_spawn"), TownsEconomyObject.getFormattedBalance(travelCost))); // + TownsEconomyObject.getEconomyCurrency()));
			}

			// If an Admin or Essentials teleport isn't being used, use our own.
			if (isTownsAdmin) {
				if (player.getVehicle() != null) {
					player.getVehicle().eject();
				}
				if (! chunk.isLoaded()) {
					chunk.load();
				}
				player.teleport(town.getSpawn());
				return;
			}

			if (plugin.getTownsUniverse().isTeleportWarmupRunning()) {
				// Use teleport warmup
				player.sendMessage(String.format(TownsSettings.getLangString("msg_town_spawn_warmup"), TownsSettings.getTeleportWarmupTime()));
				plugin.getTownsUniverse().requestTeleport(player, town, travelCost);
			} else {
				// Don't use teleport warmup
				if (player.getVehicle() != null) {
					player.getVehicle().eject();
				}
				if (! chunk.isLoaded()) {
					chunk.load();
				}
				player.teleport(town.getSpawn());
			}
		} catch (TownsException e) {
			TownsMessaging.sendErrorMsg(player, e.getMessage());
		} 
	}

	public void townDelete(Player player, String[] split) {
		if (split.length == 0) {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(player.getName());
				Town town = resident.getTown();

				if (! resident.isMayor()) {
					throw new TownsException(TownsSettings.getLangString("msg_not_mayor"));
				}
				if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.delete"))) {
					throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
				}

				plugin.getTownsUniverse().removeTown(town);
				TownsMessaging.sendGlobalMessage(TownsSettings.getDelTownMsg(town));
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		} else {
			try {
				if (! plugin.isTownsAdmin(player)) {
					throw new TownsException(TownsSettings.getLangString("msg_err_admin_only_delete_town"));
				}
				Town town = plugin.getTownsUniverse().getTown(split[0]);
				plugin.getTownsUniverse().removeTown(town);
				TownsMessaging.sendGlobalMessage(TownsSettings.getDelTownMsg(town));
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}

	/**
	 * Confirm player is a mayor or assistant, then get list of filter names
	 * with online players and kick them from town. Command: /town kick
	 * [resident] .. [resident]
	 *
	 * @param player
	 * @param names
	 */

	public void townKick(Player player, String[] names) {
		Resident resident;
		Town town;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();
			if (! resident.isMayor()) {
				if (! town.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
				}
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		townKickResidents(player, resident, town, plugin.getTownsUniverse().getValidatedResidents(player, names));

		plugin.updateCache();
	}

	/*
	   private static List<Resident> getResidents(Player player, String[] names) {
			   List<Resident> invited = new ArrayList<Resident>();
			   for (String name : names)
					   try {
							   Resident target = plugin.getTownsUniverse().getResident(name);
							   invited.add(target);
					   } catch (TownsException x) {
							   TownsMessaging.sendErrorMsg(player, x.getError());
					   }
			   return invited;
	   }
	   */
	public static void townAddResidents(Player player, Town town, List<Resident> invited) {
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident newMember : invited) {
			try {
				// only add players with the right permissions.
				if (plugin.isPermissions()) {
					if (plugin.getServer().matchPlayer(newMember.getName()).isEmpty()) { //Not online
						TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_offline_no_join"), newMember.getName()));
						remove.add(newMember);
					} else if (! TownsUniverse.getPermissionSource().hasPermission(plugin.getServer().getPlayer(newMember.getName()), "towns.town.resident")) {
						TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_not_allowed_join"), newMember.getName()));
						remove.add(newMember);
					} else {
						town.addResidentCheck(newMember);
						townInviteResident(town, newMember);
					}
				} else {
					town.addResidentCheck(newMember);
					townInviteResident(town, newMember);
				}
			} catch (AlreadyRegisteredException e) {
				remove.add(newMember);
				TownsMessaging.sendErrorMsg(player, e.getError());
			}
		}
		for (Resident newMember : remove) {
			invited.remove(newMember);
		}

		if (invited.size() > 0) {
			String msg = "";
			for (Resident newMember : invited) {
				msg += newMember.getName() + ", ";
			}

			msg = msg.substring(0, msg.length() - 2);

			msg = String.format(TownsSettings.getLangString("msg_invited_join_town"), player.getName(), msg);
			TownsMessaging.sendTownMessage(town, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveTown(town);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public static void townAddResident(Town town, Resident resident) throws AlreadyRegisteredException {
		town.addResident(resident);
		plugin.deleteCache(resident.getName());
		TownsUniverse.getDataSource().saveResident(resident);
		TownsUniverse.getDataSource().saveTown(town);
	}

	private static void townInviteResident(Town town, Resident newMember) throws AlreadyRegisteredException {
		if (TownsSettings.isUsingQuestioner()) {
			Questioner questioner = plugin.getQuestioner();
			questioner.loadClasses();

			List<Option> options = new ArrayList<Option>();
			options.add(new Option(TownsSettings.questionerAccept(), new JoinTownTask(newMember, town)));
			options.add(new Option(TownsSettings.questionerDeny(), new ResidentTownQuestionTask(newMember, town) {
				@Override
				public void run() {
					TownsMessaging.sendTownMessage(getTown(), String.format(TownsSettings.getLangString("msg_deny_invite"), getResident().getName()));
				}
			}));
			Question question = new Question(newMember.getName(), String.format(TownsSettings.getLangString("msg_invited"), town.getName()), options);
			try {
				plugin.appendQuestion(question);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			try {
				townAddResident(town, newMember);
			} catch (AlreadyRegisteredException e) {
			}
		}
	}

	public void townKickResidents(Player player, Resident resident, Town town, List<Resident> kicking) {
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident member : kicking) {
			if (resident == member || member.isMayor() || town.hasAssistant(member)) {
				remove.add(member);
			} else {
				try {
					town.removeResident(member);
					plugin.deleteCache(member.getName());
					TownsUniverse.getDataSource().saveResident(member);
				} catch (NotRegisteredException e) {
					remove.add(member);
				} catch (EmptyTownException e) {
					// You can't kick yourself and only the mayor can kick
					// assistants
					// so there will always be at least one resident.
				}
			}
		}

		for (Resident member : remove) {
			kicking.remove(member);
		}

		if (kicking.size() > 0) {
			String msg = "";
			for (Resident member : kicking) {
				msg += member.getName() + ", ";
				Player p = plugin.getServer().getPlayer(member.getName());
				if (p != null) {
					p.sendMessage(String.format(TownsSettings.getLangString("msg_kicked_by"), player.getName()));
				}
			}
			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_kicked"), player.getName(), msg);
			TownsMessaging.sendTownMessage(town, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveTown(town);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	/**
	 * Confirm player is a mayor or assistant, then get list of filter names
	 * with online players and invite them to town. Command: /town add
	 * [resident] .. [resident]
	 *
	 * @param player
	 * @param names
	 */

	public void townAssistantsAdd(Player player, String[] names) {
		Resident resident;
		Town town;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();
			if (! resident.isMayor()) {
				throw new TownsException(TownsSettings.getLangString("msg_not_mayor"));
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		townAssistantsAdd(player, town, plugin.getTownsUniverse().getValidatedResidents(player, names));
	}

	public void townAssistantsAdd(Player player, Town town, List<Resident> invited) {
		//TODO: change variable names from townAdd copypasta
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident newMember : invited) {
			try {
				town.addAssistant(newMember);
				plugin.deleteCache(newMember.getName());
				TownsUniverse.getDataSource().saveResident(newMember);
			} catch (AlreadyRegisteredException e) {
				remove.add(newMember);
			}
		}
		for (Resident newMember : remove) {
			invited.remove(newMember);
		}

		if (invited.size() > 0) {
			String msg = "";

			for (Resident newMember : invited) {
				msg += newMember.getName() + ", ";
			}
			msg = String.format(TownsSettings.getLangString("msg_raised_ass"), player.getName(), msg, "town");
			TownsMessaging.sendTownMessage(town, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveTown(town);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	/**
	 * Confirm player is a mayor or assistant, then get list of filter names
	 * with online players and kick them from town. Command: /town kick
	 * [resident] .. [resident]
	 *
	 * @param player
	 * @param names
	 */

	public void townAssistantsRemove(Player player, String[] names) {
		Resident resident;
		Town town;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();
			if (! resident.isMayor()) {
				throw new TownsException(TownsSettings.getLangString("msg_not_mayor"));
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		townAssistantsRemove(player, resident, town, plugin.getTownsUniverse().getValidatedResidents(player, names));
	}

	public void townAssistantsRemove(Player player, Resident resident, Town town, List<Resident> kicking) {
		ArrayList<Resident> remove = new ArrayList<Resident>();
		List<Resident> toKick = new ArrayList<Resident>(kicking);

		for (Resident member : toKick) {
			try {
				town.removeAssistant(member);
				plugin.deleteCache(member.getName());
				TownsUniverse.getDataSource().saveResident(member);
				TownsUniverse.getDataSource().saveTown(town);
			} catch (NotRegisteredException e) {
				remove.add(member);
			}
		}

		// remove invalid names so we don't try to send them messages
		if (remove.size() > 0) {
			for (Resident member : remove) {
				toKick.remove(member);
			}
		}

		if (toKick.size() > 0) {
			String msg = "";
			Player p;

			for (Resident member : toKick) {
				msg += member.getName() + ", ";
				p = plugin.getServer().getPlayer(member.getName());
				if (p != null) {
					p.sendMessage(String.format(TownsSettings.getLangString("msg_lowered_to_res_by"), player.getName()));
				}
			}
			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_lowered_to_res"), player.getName(), msg);
			TownsMessaging.sendTownMessage(town, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveTown(town);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	/**
	 * Confirm player is a mayor or assistant, then get list of filter names
	 * with online players and invite them to town. Command: /town add
	 * [resident] .. [resident]
	 *
	 * @param player
	 * @param specifiedTown to add to if not null
	 * @param names
	 */

	public static void townAdd(Player player, Town specifiedTown, String[] names) {
		Resident resident;
		Town town;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			if (specifiedTown == null) {
				town = resident.getTown();
			} else {
				town = specifiedTown;
			}
			if (! plugin.isTownsAdmin(player) && ! resident.isMayor() && ! town.hasAssistant(resident)) {
				throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		townAddResidents(player, town, plugin.getTownsUniverse().getValidatedResidents(player, names));

		plugin.updateCache();
	}

	// wrapper function for non friend setting of perms
	public static void setTownBlockOwnerPermissions(Player player, TownBlockOwner townBlockOwner, String[] split) {
		setTownBlockPermissions(player, townBlockOwner, townBlockOwner.getPermissions(), split, false);
	}

	public static void setTownBlockPermissions(Player player, TownBlockOwner townBlockOwner, TownsPermission perm, String[] split, boolean friend) {
		// TODO: switches
		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			player.sendMessage(ChatTools.formatTitle("/... set perm"));
			player.sendMessage(ChatTools.formatCommand("Level", "[resident/ally/outsider]", "", ""));
			player.sendMessage(ChatTools.formatCommand("Type", "[build/destroy/switch/itemuse]", "", ""));
			player.sendMessage(ChatTools.formatCommand("", "set perm", "[on/off]", "Toggle all permissions"));
			player.sendMessage(ChatTools.formatCommand("", "set perm", "[level/type] [on/off]", ""));
			player.sendMessage(ChatTools.formatCommand("", "set perm", "[level] [type] [on/off]", ""));
			if (townBlockOwner instanceof Town) {
				player.sendMessage(ChatTools.formatCommand("Eg", "/town set perm", "ally off", ""));
			}
			if (townBlockOwner instanceof Resident) {
				player.sendMessage(ChatTools.formatCommand("Eg", "/resident|plot set perm", "friend build on", ""));
			}
			player.sendMessage(String.format(TownsSettings.getLangString("plot_perms"), "'friend'", "'resident'"));
			player.sendMessage(TownsSettings.getLangString("plot_perms_1"));
		} else {
			//TownsPermission perm = townBlockOwner.getPermissions();

			// reset the friend to resident so the perm settings don't fail
			if (friend && split[0].equalsIgnoreCase("friend")) {
				split[0] = "resident";
			}

			if (split.length == 1) {
				if (split[0].equalsIgnoreCase("reset")) {
					// reset all townBlock permissions (by town/resident)
					for (TownBlock townBlock : new ArrayList<TownBlock>(townBlockOwner.getTownBlocks())) {
						if (((townBlockOwner instanceof Town) && (! townBlock.hasResident())) || ((townBlockOwner instanceof Resident) && (townBlock.hasResident()))) {
							// Reset permissions
							townBlock.setType(townBlock.getType());
						}
					}
					if (townBlockOwner instanceof Town) {
						TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_perms_reset"), "Town owned"));
					} else {
						TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_set_perms_reset"), "your"));
					}

					plugin.updateCache();
					return;
				} else {
					try {
						perm.setAll(plugin.parseOnOff(split[0]));
					} catch (Exception e) {
					}
				}
			} else if (split.length == 2) {
				try {
					boolean b = plugin.parseOnOff(split[1]);
					if (split[0].equalsIgnoreCase("resident") || split[0].equalsIgnoreCase("friend")) {
						perm.residentBuild = b;
						perm.residentDestroy = b;
						perm.residentSwitch = b;
						perm.residentItemUse = b;
					} else if (split[0].equalsIgnoreCase("outsider")) {
						perm.outsiderBuild = b;
						perm.outsiderDestroy = b;
						perm.outsiderSwitch = b;
						perm.outsiderItemUse = b;
					} else if (split[0].equalsIgnoreCase("ally")) {
						perm.allyBuild = b;
						perm.allyDestroy = b;
						perm.allySwitch = b;
						perm.allyItemUse = b;
					} else if (split[0].equalsIgnoreCase("build")) {
						perm.residentBuild = b;
						perm.outsiderBuild = b;
						perm.allyBuild = b;
					} else if (split[0].equalsIgnoreCase("destroy")) {
						perm.residentDestroy = b;
						perm.outsiderDestroy = b;
						perm.allyDestroy = b;
					} else if (split[0].equalsIgnoreCase("switch")) {
						perm.residentSwitch = b;
						perm.outsiderSwitch = b;
						perm.allySwitch = b;
					} else if (split[0].equalsIgnoreCase("itemuse")) {
						perm.residentItemUse = b;
						perm.outsiderItemUse = b;
						perm.allyItemUse = b;
					} else if (split[0].equalsIgnoreCase("pvp")) {
						perm.pvp = b;
					} else if (split[0].equalsIgnoreCase("fire")) {
						perm.fire = b;
					} else if (split[0].equalsIgnoreCase("explosion")) {
						perm.explosion = b;
					} else if (split[0].equalsIgnoreCase("mobs")) {
						perm.mobs = b;
					}
				} catch (Exception e) {
				}
			} else if (split.length == 3) {
				try {
					boolean b = plugin.parseOnOff(split[2]);
					String s = "";
					s = split[0] + split[1];
					perm.set(s, b);
				} catch (Exception e) {
				}
			}
			String perms = perm.toString();
			//change perm name to friend is this is a resident setting
			if (friend) {
				perms = perms.replaceAll("resident", "friend");
			}
			TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_set_perms"));
			TownsMessaging.sendMessage(player, (Colors.Green + " Perm: " + perm.getColourString()));
			TownsMessaging.sendMessage(player, Colors.Green + "PvP: " + ((perm.pvp) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Explosions: " + ((perm.explosion) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Firespread: " + ((perm.fire) ? Colors.Red + "ON" : Colors.LightGreen + "OFF") + Colors.Green + "  Mob Spawns: " + ((perm.mobs) ? Colors.Red + "ON" : Colors.LightGreen + "OFF"));
			plugin.updateCache();
		}
	}

	public static void parseTownClaimCommand(Player player, String[] split) {
		if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
			player.sendMessage(ChatTools.formatTitle("/town claim"));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town claim", "", TownsSettings.getLangString("msg_block_claim")));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town claim", "outpost", TownsSettings.getLangString("mayor_help_3")));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town claim", "[circle/rect] [radius]", TownsSettings.getLangString("mayor_help_4")));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town claim", "[circle/rect] auto", TownsSettings.getLangString("mayor_help_5")));
		} else {
			Resident resident;
			Town town;
			TownsWorld world;
			try {
				if (plugin.getTownsUniverse().isWarTime()) {
					throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
				}

				if (! plugin.isTownsAdmin(player) && plugin.isPermissions() && ! TownsUniverse.getPermissionSource().hasPermission(player, "towns.town.claim")) {
					throw new TownsException(TownsSettings.getLangString("msg_no_perms_claim"));
				}

				resident = plugin.getTownsUniverse().getResident(player.getName());
				town = resident.getTown();
				if (! resident.isMayor() && ! town.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
				}
				world = TownsUniverse.getWorld(player.getWorld().getName());

				if (! world.isUsingTowns()) {
					throw new TownsException(TownsSettings.getLangString("msg_set_use_towns_off"));
				}

				double blockCost = 0;
				List<WorldCoord> selection;
				boolean attachedToEdge = true;

				if (split.length == 1 && split[0].equalsIgnoreCase("outpost")) {
					if (TownsSettings.isAllowingOutposts()) {
						selection = new ArrayList<WorldCoord>();
						selection.add(new WorldCoord(world, Coord.parseCoord(plugin.getCache(player).getLastLocation())));
						blockCost = TownsSettings.getOutpostCost();
						attachedToEdge = false;
					} else {
						throw new TownsException(TownsSettings.getLangString("msg_outpost_disable"));
					}
				} else {
					selection = TownsUtil.selectWorldCoordArea(town, new WorldCoord(world, Coord.parseCoord(plugin.getCache(player).getLastLocation())), split);
					blockCost = TownsSettings.getClaimPrice();
				}

				TownsMessaging.sendDebugMsg("townClaim: Pre-Filter Selection " + Arrays.toString(selection.toArray(new WorldCoord[0])));
				selection = TownsUtil.filterTownOwnedBlocks(selection);
				TownsMessaging.sendDebugMsg("townClaim: Post-Filter Selection " + Arrays.toString(selection.toArray(new WorldCoord[0])));
				checkIfSelectionIsValid(town, selection, attachedToEdge, blockCost, false);

					double cost = blockCost * selection.size();
					if (TownsSettings.isUsingEconomy() && ! town.pay(cost, String.format("Town Claim (%d)", selection.size()))) {
						throw new TownsException(String.format(TownsSettings.getLangString("msg_no_funds_claim"), selection.size(), cost + TownsEconomyObject.getEconomyCurrency()));
					} 

				new TownClaim(plugin, player, town, selection, true, false).start();

				//for (WorldCoord worldCoord : selection)
				//		townClaim(town, worldCoord);

				//TownsUniverse.getDataSource().saveTown(town);
				//TownsUniverse.getDataSource().saveWorld(world);

				//plugin.sendMsg(player, String.format(TownsSettings.getLangString("msg_annexed_area"), Arrays.toString(selection.toArray(new WorldCoord[0]))));
				//plugin.updateCache();
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}

	public static void parseTownUnclaimCommand(Player player, String[] split) {
		if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
			player.sendMessage(ChatTools.formatTitle("/town unclaim"));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town unclaim", "", TownsSettings.getLangString("mayor_help_6")));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town unclaim", "[circle/rect] [radius]", TownsSettings.getLangString("mayor_help_7")));
			player.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/town unclaim", "all", TownsSettings.getLangString("mayor_help_8")));
		} else {
			Resident resident;
			Town town;
			TownsWorld world;
			try {
				if (plugin.getTownsUniverse().isWarTime()) {
					throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
				}

				resident = plugin.getTownsUniverse().getResident(player.getName());
				town = resident.getTown();
				if (! resident.isMayor()) {
					if (! town.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
					}
				}
				world = TownsUniverse.getWorld(player.getWorld().getName());

				List<WorldCoord> selection;
				if (split.length == 1 && split[0].equalsIgnoreCase("all")) {
					new TownClaim(plugin, player, town, null, false, false).start();
				}
				//townUnclaimAll(town);
				else {
					selection = TownsUtil.selectWorldCoordArea(town, new WorldCoord(world, Coord.parseCoord(plugin.getCache(player).getLastLocation())), split);
					selection = TownsUtil.filterOwnedBlocks(town, selection);

					// Set the area to unclaim
					new TownClaim(plugin, player, town, selection, false, false).start();

					//for (WorldCoord worldCoord : selection)
					//		townUnclaim(town, worldCoord, false);

					TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_abandoned_area"), Arrays.toString(selection.toArray(new WorldCoord[0]))));
				}
				TownsUniverse.getDataSource().saveTown(town);
				TownsUniverse.getDataSource().saveWorld(world);
				plugin.updateCache();
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}

	public static boolean isEdgeBlock(TownBlockOwner owner, List<WorldCoord> worldCoords) {
		// TODO: Better algorithm that doesn't duplicates checks.

		for (WorldCoord worldCoord : worldCoords) {
			if (isEdgeBlock(owner, worldCoord)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isEdgeBlock(TownBlockOwner owner, WorldCoord worldCoord) {
		if (TownsSettings.getDebug()) {
			System.out.print("[Towns] Debug: isEdgeBlock(" + worldCoord.toString() + ") = ");
		}

		int[][] offset = {{- 1, 0}, {1, 0}, {0, - 1}, {0, 1}};
		for (int i = 0; i < 4; i++) {
			try {
				TownBlock edgeTownBlock = worldCoord.getWorld().getTownBlock(new Coord(worldCoord.getX() + offset[i][0], worldCoord.getZ() + offset[i][1]));
				if (edgeTownBlock.isOwner(owner)) {
					if (TownsSettings.getDebug()) {
						System.out.println("true");
					}
					return true;
				}
			} catch (NotRegisteredException e) {
			}
		}
		if (TownsSettings.getDebug()) {
			System.out.println("false");
		}
		return false;
	}

	public static void checkIfSelectionIsValid(TownBlockOwner owner, List<WorldCoord> selection, boolean attachedToEdge, double blockCost, boolean force) throws TownsException {
		if (force) {
			return;
		}
		Town town = (Town) owner;

		//System.out.print("isEdgeBlock: "+ isEdgeBlock(owner, selection));

		if (attachedToEdge && ! isEdgeBlock(owner, selection) && ! town.getTownBlocks().isEmpty()) {
			if (selection.size() == 0) {
				throw new TownsException(TownsSettings.getLangString("msg_already_claimed_2"));
			} else {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_attached_edge"));
			}
		}

		if (owner instanceof Town) {
			//Town town = (Town)owner;
			int available = TownsSettings.getMaxTownBlocks(town) - town.getTownBlocks().size();
			TownsMessaging.sendDebugMsg("Claim Check Available: " + available);
			if (available - selection.size() < 0) {
				throw new TownsException(TownsSettings.getLangString("msg_err_not_enough_blocks"));
			}
		}

			double cost = blockCost * selection.size();
			if (TownsSettings.isUsingEconomy() && ! owner.canPayFromHoldings(cost)) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_err_cant_afford_blocks"), selection.size(), cost + TownsEconomyObject.getEconomyCurrency()));
			}
	}
	/*
	   public static boolean townClaim(Town town, WorldCoord worldCoord) throws TownsException {
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
					   if (!town.hasHomeBlock())
							   town.setHomeBlock(townBlock);
					   if (town.getWorld().isUsingPlotManagementRevert()) {
						   PlotBlockData plotChunk = TownsRegenAPI.getPlotChunk(townBlock);
						   if (plotChunk != null) {
							   TownsRegenAPI.deletePlotChunk(plotChunk); // just claimed so stop regeneration.
						   } else {
							   plotChunk = new PlotBlockData(townBlock); // Not regenerating so create a new snapshot.
							   plotChunk.initialize();
						   }
						   TownsRegenAPI.addPlotChunkSnapshot(plotChunk); // Save a snapshot.
						   plotChunk = null;
					   }
					   return true;
			   }
	   }

	   public static boolean townUnclaim(Town town, WorldCoord worldCoord, boolean force) throws TownsException {
			   try {
					   TownBlock townBlock = worldCoord.getTownBlock();
					   if (town != townBlock.getTown() && !force)
							   throw new TownsException(TownsSettings.getLangString("msg_area_not_own"));

					   plugin.getTownsUniverse().removeTownBlock(townBlock);

					   return true;
			   } catch (NotRegisteredException e) {
					   throw new TownsException(TownsSettings.getLangString("msg_not_claimed_1"));
			   }
	   }

	   public static boolean townUnclaimAll(Town town) {
			   plugin.getTownsUniverse().removeTownBlocks(town);
			   TownsMessaging.sendTownMessage(town, TownsSettings.getLangString("msg_abandoned_area_1"));

			   return true;
	   }
	   */

	private void townWithdraw(Player player, int amount) {
		Resident resident;
		Town town;
		try {
			if (! TownsSettings.getTownBankAllowWithdrawls()) {
				throw new TownsException(TownsSettings.getLangString("msg_err_withdraw_disabled"));
			}

			if (amount < 0) {
				throw new TownsException(TownsSettings.getLangString("msg_err_negative_money"));
			}

			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();

			town.withdrawFromBank(resident, amount);
			TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_xx_withdrew_xx"), resident.getName(), amount, "town"));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		} 
	}

	private void townDeposit(Player player, int amount) {
		Resident resident;
		Town town;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			town = resident.getTown();

			double bankcap = TownsSettings.getTownBankCap();
			if (bankcap > 0) {
				if (amount + town.getHoldingBalance() > bankcap) {
					throw new TownsException(String.format(TownsSettings.getLangString("msg_err_deposit_capped"), bankcap));
				}
			}

			if (amount < 0) {
				throw new TownsException(TownsSettings.getLangString("msg_err_negative_money"));
			}

			if (! resident.payTo(amount, town, "Town Deposit")) {
				throw new TownsException(TownsSettings.getLangString("msg_insuf_funds"));
			}

			TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, "town"));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		} 
	}
}
