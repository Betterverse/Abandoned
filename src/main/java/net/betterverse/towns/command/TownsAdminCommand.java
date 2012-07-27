package net.betterverse.towns.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyTownException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsLogger;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.TownsUtil;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.TownClaim;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.MemMgmt;
import net.betterverse.towns.util.StringMgmt;
import net.betterverse.towns.util.TimeTools;

/**
 * Send a list of all general townsadmin help commands to player Command:
 * /townsadmin
 */
public class TownsAdminCommand implements CommandExecutor {
	private static Towns plugin;
	private static final List<String> ta_help = new ArrayList<String>();
	private static final List<String> ta_panel = new ArrayList<String>();
	private static final List<String> ta_unclaim = new ArrayList<String>();

	private boolean isConsole;
	private Player player;
	private CommandSender sender;
	static {
		ta_help.add(ChatTools.formatTitle("/townsadmin"));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "", TownsSettings.getLangString("admin_panel_1")));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "set [] .. []", "'/townsadmin set' " + TownsSettings.getLangString("res_5")));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "unclaim [radius]", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "town/nation", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "givebonus [town/player] [num]", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "toggle neutral/war/debug/devmode", ""));

		//TODO: ta_help.add(ChatTools.formatCommand("", "/townsadmin", "npc rename [old name] [new name]", ""));
		//TODO: ta_help.add(ChatTools.formatCommand("", "/townsadmin", "npc list", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "reload", TownsSettings.getLangString("admin_panel_2")));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "reset", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "backup", ""));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "newday", TownsSettings.getLangString("admin_panel_3")));
		ta_help.add(ChatTools.formatCommand("", "/townsadmin", "purge [number of days]", ""));

		ta_unclaim.add(ChatTools.formatTitle("/townsadmin unclaim"));
		ta_unclaim.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin unclaim", "", TownsSettings.getLangString("townsadmin_help_1")));
		ta_unclaim.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin unclaim", "[radius]", TownsSettings.getLangString("townsadmin_help_2")));
	}

	public TownsAdminCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		this.sender = sender;

		if (sender instanceof Player) {
			player = (Player) sender;
			isConsole = false;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));

			if (! plugin.isTownsAdmin(player)) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_admin_only"));
				return true;
			}
		} else {
			isConsole = true;
			this.player = null;
		}

		try {
			parseTownsAdminCommand(args);
		} catch (TownsException e) {
			TownsMessaging.sendErrorMsg(sender, e.getMessage());
		}

		return true;
	}

	private Object getSender() {
		if (isConsole) {
			return sender;
		} else {
			return player;
		}
	}

	public void parseTownsAdminCommand(String[] split) throws TownsException {
		if (split.length == 0) {
			buildTAPanel();
			for (String line : ta_panel) {
				sender.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
			for (String line : ta_help) {
				sender.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("set")) {
			adminSet(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("town")) {
			parseAdminTownCommand(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("nation")) {
			parseAdminNationCommand(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("toggle")) {
			parseToggleCommand(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("givebonus")) {
			giveBonus(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("reload")) {
			reloadTowns(false);
		} else if (split[0].equalsIgnoreCase("reset")) {
			reloadTowns(true);
		} else if (split[0].equalsIgnoreCase("backup")) {
			try {
				TownsUniverse.getDataSource().backup();
				TownsMessaging.sendMsg(getSender(), TownsSettings.getLangString("mag_backup_success"));
			} catch (IOException e) {
				TownsMessaging.sendErrorMsg(getSender(), "Error: " + e.getMessage());
			}
		} else if (split[0].equalsIgnoreCase("newday")) {
			plugin.getTownsUniverse().newDay();
		} else if (split[0].equalsIgnoreCase("purge")) {
			purge(StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("unclaim")) {
			parseAdminUnclaimCommand(StringMgmt.remFirstArg(split));
		}
		/*
			   else if (split[0].equalsIgnoreCase("seed") && TownsSettings.getDebug())
					   seedTowns();
			   else if (split[0].equalsIgnoreCase("warseed") && TownsSettings.getDebug())
					   warSeed(player);
					   */
		else {
			TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_sub"));
		}
	}

	private void giveBonus(String[] split) throws TownsException {
		Town town;

		try {
			if (split.length != 2) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_err_invalid_input"), "Eg: givebonus [town/player] [n]"));
			}
			try {
				town = plugin.getTownsUniverse().getTown(split[0]);
			} catch (NotRegisteredException e) {
				town = plugin.getTownsUniverse().getResident(split[0]).getTown();
			}
			try {
				town.setBonusBlocks(town.getBonusBlocks() + Integer.parseInt(split[1].trim()));
				TownsMessaging.sendMsg(getSender(), String.format(TownsSettings.getLangString("msg_give_total"), town.getName(), split[1], town.getBonusBlocks()));
			} catch (NumberFormatException nfe) {
				throw new TownsException(TownsSettings.getLangString("msg_error_must_be_int"));
			}
			TownsUniverse.getDataSource().saveTown(town);
		} catch (TownsException e) {
			throw new TownsException(e.getError());
		}
	}

	private void buildTAPanel() {
		ta_panel.clear();
		Runtime run = Runtime.getRuntime();
		ta_panel.add(ChatTools.formatTitle(TownsSettings.getLangString("ta_panel_1")));
		ta_panel.add(Colors.Blue + "[" + Colors.LightBlue + "Towns" + Colors.Blue + "] " + Colors.Green + TownsSettings.getLangString("ta_panel_2") + Colors.LightGreen + plugin.getTownsUniverse().isWarTime() + Colors.Gray + " | " + Colors.Green + TownsSettings.getLangString("ta_panel_3") + (plugin.getTownsUniverse().isHealthRegenRunning() ? Colors.LightGreen + "On" : Colors.Rose + "Off") + Colors.Gray + " | " + (Colors.Green + TownsSettings.getLangString("ta_panel_5") + (plugin.getTownsUniverse().isDailyTimerRunning() ? Colors.LightGreen + "On" : Colors.Rose + "Off")));
		/*
		ta_panel.add(Colors.Blue + "[" + Colors.LightBlue + "Towns" + Colors.Blue + "] "
						+ Colors.Green + TownsSettings.getLangString("ta_panel_4")
						+ (TownsSettings.isRemovingWorldMobs() ? Colors.LightGreen + "On" : Colors.Rose + "Off")
						+ Colors.Gray + " | "
						+ Colors.Green + TownsSettings.getLangString("ta_panel_4_1")
						+ (TownsSettings.isRemovingTownMobs() ? Colors.LightGreen + "On" : Colors.Rose + "Off"));

		try {
				TownsEconomyObject.checkEconomy();
				ta_panel.add(Colors.Blue + "[" + Colors.LightBlue + "Economy" + Colors.Blue + "] "
								+ Colors.Green + TownsSettings.getLangString("ta_panel_6") + Colors.LightGreen + TownsFormatter.formatMoney(getTotalEconomy()) + Colors.Gray + " | "
								+ Colors.Green + TownsSettings.getLangString("ta_panel_7") + Colors.LightGreen + getNumBankAccounts());
		} catch (Exception e) {
		}
		*/
		ta_panel.add(Colors.Blue + "[" + Colors.LightBlue + TownsSettings.getLangString("ta_panel_8") + Colors.Blue + "] " + Colors.Green + TownsSettings.getLangString("ta_panel_9") + Colors.LightGreen + MemMgmt.getMemSize(run.totalMemory()) + Colors.Gray + " | " + Colors.Green + TownsSettings.getLangString("ta_panel_10") + Colors.LightGreen + Thread.getAllStackTraces().keySet().size() + Colors.Gray + " | " + Colors.Green + TownsSettings.getLangString("ta_panel_11") + Colors.LightGreen + TownsFormatter.getTime());
		ta_panel.add(Colors.Yellow + MemMgmt.getMemoryBar(50, run));
	}

	public void parseAdminUnclaimCommand(String[] split) {
		if (split.length == 1 && split[0].equalsIgnoreCase("?")) {
			for (String line : ta_unclaim) {
				((CommandSender) getSender()).sendMessage(line);
			}
		} else {
			if (isConsole) {
				sender.sendMessage("[Towns] InputError: This command was designed for use in game only.");
				return;
			}
			TownsWorld world;
			try {
				if (plugin.getTownsUniverse().isWarTime()) {
					throw new TownsException(TownsSettings.getLangString("msg_war_cannot_do"));
				}

				world = TownsUniverse.getWorld(player.getWorld().getName());

				List<WorldCoord> selection;
				selection = TownsUtil.selectWorldCoordArea(null, new WorldCoord(world, Coord.parseCoord(player)), split);

				new TownClaim(plugin, player, null, selection, false, true).start();
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}

	public void parseAdminTownCommand(String[] split) {
		//TODO Make this use the actual town command procedually.

		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			sender.sendMessage(ChatTools.formatTitle("/townsadmin town"));
			sender.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin town", "[town]", ""));
			sender.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin town", "[town] add/delete [] .. []", ""));
		} else {
			try {
				Town town = plugin.getTownsUniverse().getTown(split[0]);
				if (split.length == 1) {
					TownsMessaging.sendMessage(getSender(), plugin.getTownsUniverse().getStatus(town));
				} else if (split[1].equalsIgnoreCase("add")) {
					if (isConsole) {
						sender.sendMessage("[Towns] InputError: This command was designed for use in game only.");
						return;
					}
					TownCommand.townAdd(player, town, StringMgmt.remArgs(split, 2));
				}
			} catch (NotRegisteredException e) {
				TownsMessaging.sendErrorMsg(getSender(), e.getError());
			}
		}
	}

	public void parseAdminNationCommand(String[] split) {
		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			sender.sendMessage(ChatTools.formatTitle("/townsadmin nation"));
			sender.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin nation", "[nation]", ""));
			sender.sendMessage(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin nation", "[nation] add [] .. []", ""));
		} else {
			try {
				Nation nation = plugin.getTownsUniverse().getNation(split[0]);
				if (split.length == 1) {
					TownsMessaging.sendMessage(getSender(), plugin.getTownsUniverse().getStatus(nation));
				} else if (split[1].equalsIgnoreCase("add")) {
					if (isConsole) {
						sender.sendMessage("[Towns] InputError: This command was designed for use in game only.");
						return;
					}
					NationCommand.nationAdd(nation, plugin.getTownsUniverse().getTowns(StringMgmt.remArgs(split, 2)));
				}
			} catch (NotRegisteredException e) {
				TownsMessaging.sendErrorMsg(getSender(), e.getError());
			} catch (AlreadyRegisteredException e) {
				TownsMessaging.sendErrorMsg(getSender(), e.getError());
			}
		}
	}

	public void adminSet(String[] split) {
		if (split.length == 0) {
			sender.sendMessage(ChatTools.formatTitle("/townsadmin set"));
			//TODO: player.sendMessage(ChatTools.formatCommand("", "/townsadmin set", "king [nation] [king]", ""));
			sender.sendMessage(ChatTools.formatCommand("", "/townsadmin set", "mayor [town] " + TownsSettings.getLangString("town_help_2"), ""));
			sender.sendMessage(ChatTools.formatCommand("", "/townsadmin set", "mayor [town] npc", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/townsadmin set", "debugmode [on/off]", ""));
			//player.sendMessage(ChatTools.formatCommand("", "/townsadmin set", "devmode [on/off]", ""));
		} else if (split[0].equalsIgnoreCase("mayor")) {
			if (split.length < 3) {
				sender.sendMessage(ChatTools.formatTitle("/townsadmin set mayor"));
				sender.sendMessage(ChatTools.formatCommand("Eg", "/townsadmin set mayor", "[town] " + TownsSettings.getLangString("town_help_2"), ""));
				sender.sendMessage(ChatTools.formatCommand("Eg", "/townsadmin set mayor", "[town] npc", ""));
			} else {
				try {
					Resident newMayor = null;
					Town town = plugin.getTownsUniverse().getTown(split[1]);

					if (split[2].equalsIgnoreCase("npc")) {
						String name = nextNpcName();
						plugin.getTownsUniverse().newResident(name);

						newMayor = plugin.getTownsUniverse().getResident(name);

						newMayor.setRegistered(System.currentTimeMillis());
						newMayor.setLastOnline(0);
						newMayor.setNPC(true);

						TownsUniverse.getDataSource().saveResident(newMayor);
						TownsUniverse.getDataSource().saveResidentList();

						// set for no upkeep as an NPC mayor is assigned
						town.setHasUpkeep(false);
					} else {
						newMayor = plugin.getTownsUniverse().getResident(split[2]);

						//set upkeep again
						town.setHasUpkeep(true);
					}

					if (! town.hasResident(newMayor)) {
						TownCommand.townAddResident(town, newMayor);
					}
					// Delete the resident if the old mayor was an NPC.
					Resident oldMayor = town.getMayor();

					town.setMayor(newMayor);

					if (oldMayor.isNPC()) {
						try {
							town.removeResident(oldMayor);
							plugin.getTownsUniverse().removeResident(oldMayor);

							plugin.getTownsUniverse().removeResidentList(oldMayor);
						} catch (EmptyTownException e) {
							// Should never reach here as we are setting a new mayor before removing the old one.
							e.printStackTrace();
						}
					}
					TownsUniverse.getDataSource().saveTown(town);
					String[] msg = TownsSettings.getNewMayorMsg(newMayor.getName());
					TownsMessaging.sendTownMessage(town, msg);
					//TownsMessaging.sendMessage(player, msg);
				} catch (TownsException e) {
					TownsMessaging.sendErrorMsg(getSender(), e.getError());
				}
			}
		} else {
			TownsMessaging.sendErrorMsg(getSender(), String.format(TownsSettings.getLangString("msg_err_invalid_property"), "administrative"));
			return;
		}
	}

	public String nextNpcName() throws TownsException {
		String name;
		int i = 0;
		do {
			name = TownsSettings.getNPCPrefix() + ++ i;
			if (! plugin.getTownsUniverse().hasResident(name)) {
				return name;
			}
			if (i > 100000) {
				throw new TownsException(TownsSettings.getLangString("msg_err_too_many_npc"));
			}
		} while (true);
	}

	public void reloadTowns(Boolean reset) {
		if (reset) {
			TownsUniverse.getDataSource().deleteFile(plugin.getConfigPath());
		}
		TownsLogger.shutDown();

		TownsMessaging.sendMsg(sender, TownsSettings.getLangString("msg_reloaded"));
		//TownsMessaging.sendMsg(TownsSettings.getLangString("msg_reloaded"));
	}

	/**
	 * Remove residents who havn't logged in for X amount of days.
	 *
	 * @param split
	 */
	public void purge(String[] split) {
		if (split.length == 0) {
			//command was '/townsadmin purge'
			player.sendMessage(ChatTools.formatTitle("/townsadmin purge"));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin purge", "[number of days]", ""));
			player.sendMessage(ChatTools.formatCommand("", "", "Removes offline residents not seen for this duration.", ""));

			return;
		}

		int days = 1;

		try {
			days = Integer.parseInt(split[0]);
		} catch (NumberFormatException e) {
			TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_error_must_be_int"));
			return;
		}

		for (Resident resident : new ArrayList<Resident>(plugin.getTownsUniverse().getResidents())) {
			if (! resident.isNPC() && (System.currentTimeMillis() - resident.getLastOnline() > (TimeTools.getMillis(days + "d"))) && ! plugin.isOnline(resident.getName())) {
				TownsMessaging.sendMessage(this.sender, "Deleting resident: " + resident.getName());
				plugin.getTownsUniverse().removeResident(resident);
				plugin.getTownsUniverse().removeResidentList(resident);
			}
		}
	}

	public void parseToggleCommand(String[] split) throws TownsException {
		boolean choice;

		if (split.length == 0) {
			//command was '/townsadmin toggle'
			player.sendMessage(ChatTools.formatTitle("/townsadmin toggle"));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin toggle", "war", ""));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin toggle", "neutral", ""));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin toggle", "devmode", ""));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin toggle", "debug", ""));
			player.sendMessage(ChatTools.formatCommand("", "/townsadmin toggle", "townwithdraw/nationwithdraw", ""));
			return;
		} else if (split[0].equalsIgnoreCase("war")) {
			choice = plugin.getTownsUniverse().isWarTime();

			if (! choice) {
				plugin.getTownsUniverse().startWarEvent();
				TownsMessaging.sendMsg(getSender(), TownsSettings.getLangString("msg_war_started"));
			} else {
				plugin.getTownsUniverse().endWarEvent();
				TownsMessaging.sendMsg(getSender(), TownsSettings.getLangString("msg_war_ended"));
			}
		} else if (split[0].equalsIgnoreCase("neutral")) {
			try {
				choice = ! TownsSettings.isDeclaringNeutral();
				TownsSettings.setDeclaringNeutral(choice);
				TownsMessaging.sendMsg(getSender(), String.format(TownsSettings.getLangString("msg_nation_allow_neutral"), choice ? "Enabled" : "Disabled"));
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
				return;
			}
		} else if (split[0].equalsIgnoreCase("devmode")) {
			try {
				choice = ! TownsSettings.isDevMode();
				TownsSettings.setDevMode(choice);
				TownsMessaging.sendMsg(getSender(), "Dev Mode " + (choice ? Colors.Green + "Enabled" : Colors.Red + "Disabled"));
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
			}
		} else if (split[0].equalsIgnoreCase("debug")) {
			try {
				choice = ! TownsSettings.getDebug();
				TownsSettings.setDebug(choice);
				TownsMessaging.sendMsg(getSender(), "Debug Mode " + (choice ? Colors.Green + "Enabled" : Colors.Red + "Disabled"));
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
			}
		} else if (split[0].equalsIgnoreCase("townwithdraw")) {
			try {
				choice = ! TownsSettings.getTownBankAllowWithdrawls();
				TownsSettings.SetTownBankAllowWithdrawls(choice);
				TownsMessaging.sendMsg(getSender(), "Town Withdrawls " + (choice ? Colors.Green + "Enabled" : Colors.Red + "Disabled"));
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
			}
		} else if (split[0].equalsIgnoreCase("nationwithdraw")) {
			try {
				choice = ! TownsSettings.geNationBankAllowWithdrawls();
				TownsSettings.SetNationBankAllowWithdrawls(choice);
				TownsMessaging.sendMsg(getSender(), "Nation Withdrawls " + (choice ? Colors.Green + "Enabled" : Colors.Red + "Disabled"));
			} catch (Exception e) {
				TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
			}
		} else {
			// parameter error message
			// neutral/war/townmobs/worldmobs
			TownsMessaging.sendErrorMsg(getSender(), TownsSettings.getLangString("msg_err_invalid_choice"));
		}
	}
	/*
	private void warSeed(Player player) {
			Resident r1 = plugin.getTownsUniverse().newResident("r1");
			Resident r2 = plugin.getTownsUniverse().newResident("r2");
			Resident r3 = plugin.getTownsUniverse().newResident("r3");
			Coord key = Coord.parseCoord(player);
			Town t1 = newTown(plugin.getTownsUniverse(), player.getWorld(), "t1", r1, key, player.getLocation());
			Town t2 = newTown(plugin.getTownsUniverse(), player.getWorld(), "t2", r2, new Coord(key.getX() + 1, key.getZ()), player.getLocation());
			Town t3 = newTown(plugin.getTownsUniverse(), player.getWorld(), "t3", r3, new Coord(key.getX(), key.getZ() + 1), player.getLocation());
			Nation n1 =

	}

	public void seedTowns() {
			TownsUniverse townsUniverse = plugin.getTownsUniverse();
			Random r = new Random();
			for (int i = 0; i < 1000; i++) {
					try {
							townsUniverse.newNation(Integer.toString(r.nextInt()));
					} catch (TownsException e) {
					}
					try {
							townsUniverse.newTown(Integer.toString(r.nextInt()));
					} catch (TownsException e) {
					}
					try {
							townsUniverse.newResident(Integer.toString(r.nextInt()));
					} catch (TownsException e) {
					}
			}
	}

	private static double getTotalEconomy() {
			double total = 0;
			try {
					return total;
			} catch (Exception e) {
			}
			return total;
	}

	private static int getNumBankAccounts() {
			try {
					return 0;
			} catch (Exception e) {
					return 0;
			}
	}
	 */
}
