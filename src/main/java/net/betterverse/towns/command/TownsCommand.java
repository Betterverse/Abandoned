package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsAsciiMap;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.TownsUtil;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.ResidentList;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlockOwner;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsObject;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.KeyValue;
import net.betterverse.towns.util.KeyValueTable;
import net.betterverse.towns.util.StringMgmt;
import net.betterverse.towns.util.TimeMgmt;

public class TownsCommand implements CommandExecutor {
	//protected static TownsUniverse universe;
	private static Towns plugin;

	private static final List<String> towns_general_help = new ArrayList<String>();
	private static final List<String> towns_help = new ArrayList<String>();
	private static final List<String> towns_top = new ArrayList<String>();
	private static final List<String> towns_war = new ArrayList<String>();
	private static String towns_version;
	static {
		towns_general_help.add(ChatTools.formatTitle(TownsSettings.getLangString("help_0")));
		towns_general_help.add(TownsSettings.getLangString("help_1"));
		towns_general_help.add(ChatTools.formatCommand("", "/resident", "?", "") + ", " + ChatTools.formatCommand("", "/town", "?", "") + ", " + ChatTools.formatCommand("", "/nation", "?", "") + ", " + ChatTools.formatCommand("", "/plot", "?", "") + ", " + ChatTools.formatCommand("", "/towns", "?", ""));
		towns_general_help.add(ChatTools.formatCommand("", "/townchat", " [msg]", TownsSettings.getLangString("help_2")) + ", " + ChatTools.formatCommand("", "/nationchat", " [msg]", TownsSettings.getLangString("help_3")));
		towns_general_help.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/townsadmin", "?", ""));

		towns_help.add(ChatTools.formatTitle("/towns"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "", "General help for Towns"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "map", "Displays a map of the nearby townblocks"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "prices", "Display the prices used with Economy"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "top", "Display highscores"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "time", "Display time until a new day"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "universe", "Displays stats"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "v", "Displays the version of Towns"));
		towns_help.add(ChatTools.formatCommand("", "/towns", "war", "'/towns war' for more info"));
	}

	public TownsCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		towns_version = Colors.Green + "Towns version: " + Colors.LightGreen + plugin.getTownsUniverse().getPlugin().getVersion();

		towns_war.add(ChatTools.formatTitle("/towns war"));
		towns_war.add(ChatTools.formatCommand("", "/towns war", "stats", ""));
		towns_war.add(ChatTools.formatCommand("", "/towns war", "scores", ""));

		if (sender instanceof Player) {
			Player player = (Player) sender;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));
			parseTownsCommand(player, args);
		} else {
			// Console output
			if (args.length == 0) {
				for (String line : towns_general_help) {
					sender.sendMessage(Colors.strip(line));
				}
			} else if (args[0].equalsIgnoreCase("tree")) {
				plugin.getTownsUniverse().sendUniverseTree(sender);
			} else if (args[0].equalsIgnoreCase("time")) {
				TownsMessaging.sendMsg("Time until a New Day: " + TimeMgmt.formatCountdownTime(TownsUtil.townsTime()));
			} else if (args[0].equalsIgnoreCase("version") || args[0].equalsIgnoreCase("v")) {
				sender.sendMessage(Colors.strip(towns_version));
			} else if (args[0].equalsIgnoreCase("war")) {
				boolean war = TownsWar(StringMgmt.remFirstArg(args));
				for (String line : towns_war) {
					sender.sendMessage(Colors.strip(line));
				}
				if (! war) {
					sender.sendMessage("The world isn't currently at war.");
				}

				towns_war.clear();
			} else if (args[0].equalsIgnoreCase("universe")) {
				for (String line : getUniverseStats()) {
					sender.sendMessage(Colors.strip(line));
				}
			}
		}
		return true;
	}

	private void parseTownsCommand(Player player, String[] split) {
		if (split.length == 0) {
			for (String line : towns_general_help) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
			for (String line : towns_help) {
				player.sendMessage(Colors.strip(line));
			}
		} else if (split[0].equalsIgnoreCase("map")) {
			if (split.length > 1 && split[1].equalsIgnoreCase("big")) {
				TownsAsciiMap.generateAndSend(plugin, player, 18);
			} else {
				showMap(player);
			}
		} else if (split[0].equalsIgnoreCase("prices")) {
			Town town = null;
			if (split.length > 1) {
				try {
					town = plugin.getTownsUniverse().getTown(split[1]);
				} catch (NotRegisteredException x) {
					sendErrorMsg(player, x.getError());
					return;
				}
			} else if (split.length == 0) {
				try {
					Resident resident = plugin.getTownsUniverse().getResident(player.getName());
					town = resident.getTown();
				} catch (NotRegisteredException x) {
				}
			}

			for (String line : getTownsPrices(town)) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("top")) {
			TopCommand(player, StringMgmt.remFirstArg(split));
		} else if (split[0].equalsIgnoreCase("tree")) {
			consoleUseOnly(player);
		} else if (split[0].equalsIgnoreCase("time")) {
			TownsMessaging.sendMsg(player, "Time until a New Day: " + TimeMgmt.formatCountdownTime(TownsUtil.townsTime()));
		} else if (split[0].equalsIgnoreCase("universe")) {
			for (String line : getUniverseStats()) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("version") || split[0].equalsIgnoreCase("v")) {
			player.sendMessage(towns_version);
		} else if (split[0].equalsIgnoreCase("war")) {
			boolean war = TownsWar(StringMgmt.remFirstArg(split));
			for (String line : towns_war) {
				player.sendMessage(Colors.strip(line));
			}
			if (! war) {
				sendErrorMsg(player, "The world isn't currently at war.");
			}

			towns_war.clear();
		} else {
			sendErrorMsg(player, "Invalid sub command.");
		}
	}

	private boolean TownsWar(String[] args) {
		if (plugin.getTownsUniverse().isWarTime() && args.length > 0) {
			towns_war.clear();
			if (args[0].equalsIgnoreCase("stats")) {
				towns_war.addAll(plugin.getTownsUniverse().getWarEvent().getStats());
			} else if (args[0].equalsIgnoreCase("scores")) {
				towns_war.addAll(plugin.getTownsUniverse().getWarEvent().getScores(- 1));
			}
		}

		return plugin.getTownsUniverse().isWarTime();
	}

	private void TopCommand(Player player, String[] args) {
		if (! plugin.isTownsAdmin(player) && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.top"))) {
			sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
			return;
		}

		if (args.length == 0 || args[0].equalsIgnoreCase("?")) {
			towns_top.add(ChatTools.formatTitle("/towns top"));
			towns_top.add(ChatTools.formatCommand("", "/towns top", "money [all/resident/town/nation]", ""));
			towns_top.add(ChatTools.formatCommand("", "/towns top", "residents [all/town/nation]", ""));
			towns_top.add(ChatTools.formatCommand("", "/towns top", "land [all/resident/town]", ""));
		} else if (args[0].equalsIgnoreCase("money")) {
				if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
					List<TownsEconomyObject> list = new ArrayList<TownsEconomyObject>(plugin.getTownsUniverse().getResidents());
					list.addAll(plugin.getTownsUniverse().getTowns());
					list.addAll(plugin.getTownsUniverse().getNations());
					towns_top.add(ChatTools.formatTitle("Top Bank Accounts"));
					towns_top.addAll(getTopBankBalance(list, 10));
				} else if (args[1].equalsIgnoreCase("resident")) {
					towns_top.add(ChatTools.formatTitle("Top Resident Bank Accounts"));
					towns_top.addAll(getTopBankBalance(new ArrayList<TownsEconomyObject>(plugin.getTownsUniverse().getResidents()), 10));
				} else if (args[1].equalsIgnoreCase("town")) {
					towns_top.add(ChatTools.formatTitle("Top Town Bank Accounts"));
					towns_top.addAll(getTopBankBalance(new ArrayList<TownsEconomyObject>(plugin.getTownsUniverse().getTowns()), 10));
				} else if (args[1].equalsIgnoreCase("nation")) {
					towns_top.add(ChatTools.formatTitle("Top Nation Bank Accounts"));
					towns_top.addAll(getTopBankBalance(new ArrayList<TownsEconomyObject>(plugin.getTownsUniverse().getNations()), 10));
				} else {
					sendErrorMsg(player, "Invalid sub command.");
				}
		} else if (args[0].equalsIgnoreCase("residents")) {
			if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
				List<ResidentList> list = new ArrayList<ResidentList>(plugin.getTownsUniverse().getTowns());
				list.addAll(plugin.getTownsUniverse().getNations());
				towns_top.add(ChatTools.formatTitle("Most Residents"));
				towns_top.addAll(getMostResidents(list, 10));
			} else if (args[1].equalsIgnoreCase("town")) {
				towns_top.add(ChatTools.formatTitle("Most Residents in a Town"));
				towns_top.addAll(getMostResidents(new ArrayList<ResidentList>(plugin.getTownsUniverse().getTowns()), 10));
			} else if (args[1].equalsIgnoreCase("nation")) {
				towns_top.add(ChatTools.formatTitle("Most Residents in a Nation"));
				towns_top.addAll(getMostResidents(new ArrayList<ResidentList>(plugin.getTownsUniverse().getNations()), 10));
			} else {
				sendErrorMsg(player, "Invalid sub command.");
			}
		} else if (args[0].equalsIgnoreCase("land")) {
			if (args.length == 1 || args[1].equalsIgnoreCase("all")) {
				List<TownBlockOwner> list = new ArrayList<TownBlockOwner>(plugin.getTownsUniverse().getResidents());
				list.addAll(plugin.getTownsUniverse().getTowns());
				towns_top.add(ChatTools.formatTitle("Most Land Owned"));
				towns_top.addAll(getMostLand(list, 10));
			} else if (args[1].equalsIgnoreCase("resident")) {
				towns_top.add(ChatTools.formatTitle("Most Land Owned by Resident"));
				towns_top.addAll(getMostLand(new ArrayList<TownBlockOwner>(plugin.getTownsUniverse().getResidents()), 10));
			} else if (args[1].equalsIgnoreCase("town")) {
				towns_top.add(ChatTools.formatTitle("Most Land Owned by Town"));
				towns_top.addAll(getMostLand(new ArrayList<TownBlockOwner>(plugin.getTownsUniverse().getTowns()), 10));
			} else {
				sendErrorMsg(player, "Invalid sub command.");
			}
		} else {
			sendErrorMsg(player, "Invalid sub command.");
		}

		for (String line : towns_top) {
			player.sendMessage(line);
		}

		towns_top.clear();
	}

	public List<String> getUniverseStats() {
		List<String> output = new ArrayList<String>();
		output.add("�0-�4###�0---�4###�0-");
		output.add("�4#�c###�4#�0-�4#�c###�4#�0   �6[�eTowns " + plugin.getTownsUniverse().getPlugin().getVersion() + "�6]");
		output.add("�4#�c####�4#�c####�4#   �3By: �bChris H (Shade)/Llmdl/ElgarL");
		output.add("�0-�4#�c#######�4#�0-");
		output.add("�0--�4##�c###�4##�0-- " + "�3Residents: �b" + Integer.toString(plugin.getTownsUniverse().getResidents().size()) + Colors.Gray + " | " + "�3Towns: �b" + Integer.toString(plugin.getTownsUniverse().getTowns().size()) + Colors.Gray + " | " + "�3Nations: �b" + Integer.toString(plugin.getTownsUniverse().getNations().size()));
		output.add("�0----�4#�c#�4#�0---- " + "�3Worlds: �b" + Integer.toString(plugin.getTownsUniverse().getWorlds().size()) + Colors.Gray + " | " + "�3TownBlocks: �b" + Integer.toString(plugin.getTownsUniverse().getAllTownBlocks().size()));
		output.add("�0-----�4#�0----- ");
		return output;
	}

	/**
	 * Send a map of the nearby townblocks status to player Command: /towns map
	 *
	 * @param player
	 */

	public static void showMap(Player player) {
		TownsAsciiMap.generateAndSend(plugin, player, 7);
	}

	/**
	 * Send the list of costs for Economy to player Command: /towns prices
	 *
	 * @param town
	 */

	/*
			 * [New] Town: 100 | Nation: 500
			 * [Upkeep] Town: 10 | Nation: 100
			 * Town [Elden]:
			 *	 [Price] Plot: 100 | Outpost: 250
			 *	 [Upkeep] Resident: 20 | Plot: 50
			 * Nation [Albion]:
			 *	 [Upkeep] Town: 100 | Neutrality: 100
			 */

	//TODO: Proceduralize and make parse function for /towns prices [town]
	public List<String> getTownsPrices(Town town) {
		List<String> output = new ArrayList<String>();
		Nation nation = null;

		if (town != null) {
			if (town.hasNation()) {
				try {
					nation = town.getNation();
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		output.add(ChatTools.formatTitle("Prices"));
		output.add(Colors.Yellow + "[New] " + Colors.Green + "Town: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getNewTownPrice()) + Colors.Gray + " | " + Colors.Green + "Nation: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getNewNationPrice()));
		output.add(Colors.Yellow + "[Upkeep] " + Colors.Green + "Town: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getTownUpkeepCost(town)) + Colors.Gray + " | " + Colors.Green + "Nation: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getNationUpkeepCost(nation)));
		output.add(Colors.Gray + "Town upkeep is based on " + Colors.LightGreen + " the " + (TownsSettings.isUpkeepByPlot() ? " number of plots" : " town level (num residents)."));

		if (town != null) {
			output.add(Colors.Yellow + "Town [" + TownsFormatter.getFormattedName(town) + "]");
			output.add(Colors.Rose + "	[Price] " + Colors.Green + "Plot: " + Colors.LightGreen + Double.toString(town.getPlotPrice()) + Colors.Gray + " | " + Colors.Green + "Outpost: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getOutpostCost()));
			output.add(Colors.Rose + "	[Upkeep] " + Colors.Green + "Resident: " + Colors.LightGreen + Double.toString(town.getTaxes()) + Colors.Gray + " | " + Colors.Green + "Plot: " + Colors.LightGreen + Double.toString(town.getPlotTax()));

			if (nation != null) {
				output.add(Colors.Yellow + "Nation [" + TownsFormatter.getFormattedName(nation) + "]");
				output.add(Colors.Rose + "	[Upkeep] " + Colors.Green + "Town: " + Colors.LightGreen + Double.toString(nation.getTaxes()) + Colors.Gray + " | " + Colors.Green + "Neutrality: " + Colors.LightGreen + TownsFormatter.formatMoney(TownsSettings.getNationNeutralityCost()));
			}
		}
		return output;
	}

	public List<String> getTopBankBalance(List<TownsEconomyObject> list, int maxListing) {
		List<String> output = new ArrayList<String>();
		KeyValueTable<TownsEconomyObject, Double> kvTable = new KeyValueTable<TownsEconomyObject, Double>();
		for (TownsEconomyObject obj : list) {
			kvTable.put(obj, obj.getHoldingBalance());
		}
		kvTable.sortByValue();
		kvTable.revese();
		int n = 0;
		for (KeyValue<TownsEconomyObject, Double> kv : kvTable.getKeyValues()) {
			n++;
			if (maxListing != - 1 && n > maxListing) {
				break;
			}
			TownsEconomyObject town = (TownsEconomyObject) kv.key;
			output.add(String.format(Colors.LightGray + "%-20s " + Colors.Gold + "|" + Colors.Blue + " %s", TownsFormatter.getFormattedName(town), TownsFormatter.formatMoney((Double) kv.value)));
		}
		return output;
	}

	public List<String> getMostResidents(List<ResidentList> list, int maxListing) {
		List<String> output = new ArrayList<String>();
		KeyValueTable<ResidentList, Integer> kvTable = new KeyValueTable<ResidentList, Integer>();
		for (ResidentList obj : list) {
			kvTable.put(obj, obj.getResidents().size());
		}
		kvTable.sortByValue();
		kvTable.revese();
		int n = 0;
		for (KeyValue<ResidentList, Integer> kv : kvTable.getKeyValues()) {
			n++;
			if (maxListing != - 1 && n > maxListing) {
				break;
			}
			ResidentList residentList = (ResidentList) kv.key;
			output.add(String.format(Colors.Blue + "%30s " + Colors.Gold + "|" + Colors.LightGray + " %10d", TownsFormatter.getFormattedName((TownsObject) residentList), (Integer) kv.value));
		}
		return output;
	}

	public List<String> getMostLand(List<TownBlockOwner> list, int maxListing) {
		List<String> output = new ArrayList<String>();
		KeyValueTable<TownBlockOwner, Integer> kvTable = new KeyValueTable<TownBlockOwner, Integer>();
		for (TownBlockOwner obj : list) {
			kvTable.put(obj, obj.getTownBlocks().size());
		}
		kvTable.sortByValue();
		kvTable.revese();
		int n = 0;
		for (KeyValue<TownBlockOwner, Integer> kv : kvTable.getKeyValues()) {
			n++;
			if (maxListing != - 1 && n > maxListing) {
				break;
			}
			TownBlockOwner town = (TownBlockOwner) kv.key;
			output.add(String.format(Colors.Blue + "%30s " + Colors.Gold + "|" + Colors.LightGray + " %10d", TownsFormatter.getFormattedName(town), (Integer) kv.value));
		}
		return output;
	}

	public void consoleUseOnly(Player player) {
		TownsMessaging.sendErrorMsg(player, "This command was designed for use in the console only.");
	}

	public void inGameUseOnly(CommandSender sender) {
		sender.sendMessage("[Towns] InputError: This command was designed for use in game only.");
	}

	public boolean sendErrorMsg(CommandSender sender, String msg) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			TownsMessaging.sendErrorMsg(player, msg);
		} else
		// Console
		{
			sender.sendMessage("[Towns] ConsoleError: " + msg);
		}

		return false;
	}
}
