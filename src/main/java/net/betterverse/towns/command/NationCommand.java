package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.List;
import javax.naming.InvalidNameException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.questioner.Questioner;
import net.betterverse.questioner.questionmanager.Option;
import net.betterverse.questioner.questionmanager.Question;
import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyNationException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.questioner.JoinNationTask;
import net.betterverse.towns.questioner.ResidentNationQuestionTask;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.StringMgmt;

/**
 * Send a list of all nation commands to player Command: /nation ?
 *
 * @param player handles all nation based commands
 */
public class NationCommand implements CommandExecutor {
	private static Towns plugin;
	private static final List<String> nation_help = new ArrayList<String>();
	private static final List<String> king_help = new ArrayList<String>();
	static {
		nation_help.add(ChatTools.formatTitle("/nation"));
		nation_help.add(ChatTools.formatCommand("", "/nation", "", TownsSettings.getLangString("nation_help_1")));
		nation_help.add(ChatTools.formatCommand("", "/nation", TownsSettings.getLangString("nation_help_2"), TownsSettings.getLangString("nation_help_3")));
		nation_help.add(ChatTools.formatCommand("", "/nation", "list", TownsSettings.getLangString("nation_help_4")));
		nation_help.add(ChatTools.formatCommand("", "/nation", "online", TownsSettings.getLangString("nation_help_9")));
		nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("res_sing"), "/nation", "deposit [$]", ""));
		nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/nation", "leave", TownsSettings.getLangString("nation_help_5")));
		if (! TownsSettings.isNationCreationAdminOnly()) {
			nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("mayor_sing"), "/nation", "new " + TownsSettings.getLangString("nation_help_2"), TownsSettings.getLangString("nation_help_6")));
		}
		nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "king ?", TownsSettings.getLangString("nation_help_7")));
		nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/nation", "new " + TownsSettings.getLangString("nation_help_2") + " [capital]", TownsSettings.getLangString("nation_help_8")));
		nation_help.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/nation", "delete " + TownsSettings.getLangString("nation_help_2"), ""));

		king_help.add(ChatTools.formatTitle(TownsSettings.getLangString("king_help_1")));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "withdraw [$]", ""));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "[add/kick] [town] .. [town]", ""));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "assistant [add/remove] " + TownsSettings.getLangString("res_2"), TownsSettings.getLangString("res_6")));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "assistant [add+/remove+] " + TownsSettings.getLangString("res_2"), TownsSettings.getLangString("res_7")));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "set [] .. []", ""));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "toggle [] .. []", ""));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "ally [add/remove] " + TownsSettings.getLangString("nation_help_2"), TownsSettings.getLangString("king_help_2")));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "enemy [add/remove] " + TownsSettings.getLangString("nation_help_2"), TownsSettings.getLangString("king_help_3")));
		king_help.add(ChatTools.formatCommand(TownsSettings.getLangString("king_sing"), "/nation", "delete", ""));
	}

	public NationCommand(Towns instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			System.out.println("[PLAYER_COMMAND] " + player.getName() + ": /" + commandLabel + " " + StringMgmt.join(args));
			if (args == null) {
				for (String line : nation_help) {
					player.sendMessage(line);
				}
				parseNationCommand(player, args);
			} else {
				parseNationCommand(player, args);
			}
		} else
		// Console
		{
			for (String line : nation_help) {
				sender.sendMessage(Colors.strip(line));
			}
		}
		return true;
	}

	public void parseNationCommand(Player player, String[] split) {
		String nationCom = "/nation";

		if (split.length == 0) {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(player.getName());
				Town town = resident.getTown();
				Nation nation = town.getNation();
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(nation));
			} catch (NotRegisteredException x) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_dont_belong_nation"));
			}
		} else if (split[0].equalsIgnoreCase("?")) {
			for (String line : nation_help) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("list")) {
			listNations(player);
		} else if (split[0].equalsIgnoreCase("new")) {
			// TODO: Make an overloaded function
			// newNation(Player,String,Town)
			if (split.length == 1) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_specify_nation_name"));
			} else if (split.length == 2) {
				try { // TODO: Make sure of the error catching
					Resident resident = plugin.getTownsUniverse().getResident(player.getName());
					if (! resident.isMayor() && ! resident.getTown().hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_peasant_right"));
					}
					newNation(player, split[1], resident.getTown().getName());
				} catch (TownsException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			} else
			// TODO: Check if player is an admin
			{
				newNation(player, split[1], split[2]);
			}
		} else if (split[0].equalsIgnoreCase("leave")) {
			nationLeave(player);
		} else if (split[0].equalsIgnoreCase("withdraw")) {
			if (split.length == 2) {
				try {
					nationWithdraw(player, Integer.parseInt(split[1].trim()));
				} catch (NumberFormatException e) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_int"));
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_must_specify_amnt"), nationCom));
			}
		} else if (split[0].equalsIgnoreCase("deposit")) {
			if (split.length == 2) {
				try {
					nationDeposit(player, Integer.parseInt(split[1].trim()));
				} catch (NumberFormatException e) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_int"));
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_must_specify_amnt"), nationCom + " deposit"));
			}
		} else {
			String[] newSplit = StringMgmt.remFirstArg(split);

			if (split[0].equalsIgnoreCase("king")) {
				nationKing(player, newSplit);
			} else if (split[0].equalsIgnoreCase("add")) {
				nationAdd(player, newSplit);
			} else if (split[0].equalsIgnoreCase("kick")) {
				nationKick(player, newSplit);
			} else if (split[0].equalsIgnoreCase("assistant")) {
				nationAssistant(player, newSplit);
			} else if (split[0].equalsIgnoreCase("set")) {
				nationSet(player, newSplit);
			} else if (split[0].equalsIgnoreCase("toggle")) {
				nationToggle(player, newSplit);
			} else if (split[0].equalsIgnoreCase("ally")) {
				nationAlly(player, newSplit);
			} else if (split[0].equalsIgnoreCase("enemy")) {
				nationEnemy(player, newSplit);
			} else if (split[0].equalsIgnoreCase("delete")) {
				nationDelete(player, newSplit);
			} else if (split[0].equalsIgnoreCase("online")) {
				try {
					Resident resident = plugin.getTownsUniverse().getResident(player.getName());
					Town town = resident.getTown();
					Nation nation = town.getNation();
					TownsMessaging.sendMessage(player, TownsFormatter.getFormattedOnlineResidents(plugin, TownsSettings.getLangString("msg_nation_online"), nation));
				} catch (NotRegisteredException x) {
					TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_dont_belong_nation"));
				}
			} else {
				try {
					Nation nation = plugin.getTownsUniverse().getNation(split[0]);
					TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(nation));
				} catch (NotRegisteredException x) {
					TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_registered_1"), split[0]));
				}
			}
		}
	}

	private void nationWithdraw(Player player, int amount) {
		Resident resident;
		Nation nation;
		try {
			if (! TownsSettings.geNationBankAllowWithdrawls()) {
				throw new TownsException(TownsSettings.getLangString("msg_err_withdraw_disabled"));
			}

			if (amount < 0) {
				throw new TownsException(TownsSettings.getLangString("msg_err_negative_money")); //TODO
			}

			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();

			nation.withdrawFromBank(resident, amount);
			TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_xx_withdrew_xx"), resident.getName(), amount, "nation"));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		} 
	}

	private void nationDeposit(Player player, int amount) {
		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();

			double bankcap = TownsSettings.getNationBankCap();
			if (bankcap > 0) {
				if (amount + nation.getHoldingBalance() > bankcap) {
					throw new TownsException(String.format(TownsSettings.getLangString("msg_err_deposit_capped"), bankcap));
				}
			}

			if (amount < 0) {
				throw new TownsException(TownsSettings.getLangString("msg_err_negative_money"));
			}

			if (! resident.payTo(amount, nation, "Nation Deposit")) {
				throw new TownsException(TownsSettings.getLangString("msg_insuf_funds"));
			}

			TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_xx_deposited_xx"), resident.getName(), amount, "nation"));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}

	/**
	 * Send a list of all nations in the universe to player Command: /nation
	 * list
	 *
	 * @param player
	 */

	public void listNations(Player player) {
		player.sendMessage(ChatTools.formatTitle(TownsSettings.getLangString("nation_plu")));
		ArrayList<String> formatedList = new ArrayList<String>();
		for (Nation nation : plugin.getTownsUniverse().getNations()) {
			formatedList.add(Colors.LightBlue + nation.getName() + Colors.Blue + " [" + nation.getNumTowns() + "]" + Colors.White);
		}
		for (String line : ChatTools.list(formatedList)) {
			player.sendMessage(line);
		}
	}

	/**
	 * Create a new nation. Command: /nation new [nation] *[capital]
	 *
	 * @param player
	 */

	public void newNation(Player player, String name, String capitalName) {
		TownsUniverse universe = plugin.getTownsUniverse();
		try {
			if (! plugin.isTownsAdmin(player) && (TownsSettings.isNationCreationAdminOnly() || (plugin.isPermissions() && ! TownsUniverse.getPermissionSource().hasPermission(player, "towns.nation.new")))) {
				throw new TownsException(TownsSettings.getNotPermToNewNationLine());
			}

			Town town = universe.getTown(capitalName);
			if (town.hasNation()) {
				throw new TownsException(TownsSettings.getLangString("msg_err_already_nation"));
			}

			if (! TownsSettings.isValidRegionName(name)) {
				throw new TownsException(String.format(TownsSettings.getLangString("msg_err_invalid_name"), name));
			}

			if (TownsSettings.isUsingEconomy() && ! town.pay(TownsSettings.getNewNationPrice(), "New Nation Cost")) {
				throw new TownsException(TownsSettings.getLangString("msg_no_funds_new_nation"));
			}

			newNation(universe, name, town);
			/*universe.newNation(name);
			Nation nation = universe.getNation(name);
			nation.addTown(town);
			nation.setCapital(town);

			universe.getDataSource().saveTown(town);
			universe.getDataSource().saveNation(nation);
			universe.getDataSource().saveNationList();*/

			TownsMessaging.sendGlobalMessage(TownsSettings.getNewNationMsg(player.getName(), name));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			// TODO: delete town data that might have been done
		} 
	}

	public Nation newNation(TownsUniverse universe, String name, Town town) throws AlreadyRegisteredException, NotRegisteredException {
		universe.newNation(name);
		Nation nation = universe.getNation(name);
		nation.addTown(town);
		nation.setCapital(town);
		if (TownsSettings.isUsingEconomy()) {
			nation.setBalance(0);
		}
		TownsUniverse.getDataSource().saveTown(town);
		TownsUniverse.getDataSource().saveNation(nation);
		TownsUniverse.getDataSource().saveNationList();

		return nation;
	}

	public void nationLeave(Player player) {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());
			Town town = resident.getTown();
			Nation nation = town.getNation();
			if (! resident.isMayor()) {
				if (! town.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_mayor_ass"));
				}
			}

			nation.removeTown(town);

			TownsUniverse.getDataSource().saveTown(town);
			TownsUniverse.getDataSource().saveNation(nation);
			TownsUniverse.getDataSource().saveNationList();

			TownsMessaging.sendNationMessage(nation, ChatTools.color(String.format(TownsSettings.getLangString("msg_nation_town_left"), town.getName())));
			TownsMessaging.sendTownMessage(town, ChatTools.color(String.format(TownsSettings.getLangString("msg_town_left_nation"), nation.getName())));
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		} catch (EmptyNationException en) {
			plugin.getTownsUniverse().removeNation(en.getNation());
			TownsUniverse.getDataSource().saveNationList();
			TownsMessaging.sendGlobalMessage(ChatTools.color(String.format(TownsSettings.getLangString("msg_del_nation"), en.getNation().getName())));
		}
	}

	public void nationDelete(Player player, String[] split) {
		if (split.length == 0) {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(player.getName());
				Town town = resident.getTown();
				Nation nation = town.getNation();

				if (! resident.isKing()) {
					throw new TownsException(TownsSettings.getLangString("msg_not_king"));
				}
				if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.nation.delete"))) {
					throw new TownsException(TownsSettings.getLangString("msg_err_command_disable"));
				}

				plugin.getTownsUniverse().removeNation(nation);
				TownsMessaging.sendGlobalMessage(TownsSettings.getDelNationMsg(nation));
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		} else {
			try {
				if (! plugin.isTownsAdmin(player)) {
					throw new TownsException(TownsSettings.getLangString("msg_err_admin_only_delete_nation"));
				}
				Nation nation = plugin.getTownsUniverse().getNation(split[0]);
				plugin.getTownsUniverse().removeNation(nation);
				TownsMessaging.sendGlobalMessage(TownsSettings.getDelNationMsg(nation));
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}

	public void nationKing(Player player, String[] split) {
		if (split.length == 0 || split[0].equalsIgnoreCase("?")) {
			for (String line : king_help) {
				player.sendMessage(line);
			}
		}
	}

	public void nationAdd(Player player, String[] names) {
		if (names.length < 1) {
			TownsMessaging.sendErrorMsg(player, "Eg: /nation add [names]");
			return;
		}

		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				if (! nation.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
				}
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		nationAdd(player, nation, plugin.getTownsUniverse().getTowns(names));
	}

	public static void nationAdd(Player player, Nation nation, List<Town> invited) {
		ArrayList<Town> remove = new ArrayList<Town>();
		for (Town town : invited) {
			try {
				//nation.addTown(town);

				nationInviteTown(player, nation, town);
			} catch (AlreadyRegisteredException e) {
				remove.add(town);
			}
		}
		for (Town town : remove) {
			invited.remove(town);
		}

		if (invited.size() > 0) {
			String msg = "";

			for (Town town : invited) {
				msg += town.getName() + ", ";
			}

			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_invited_join_nation"), player.getName(), msg);
			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			//plugin.getTownsUniverse().getDataSource().saveNation(nation);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	private static void nationInviteTown(Player player, Nation nation, Town town) throws AlreadyRegisteredException {
		Resident townMayor = town.getMayor();

		if (TownsSettings.isUsingQuestioner()) {
			Questioner questioner = plugin.getQuestioner();
			questioner.loadClasses();

			List<Option> options = new ArrayList<Option>();
			options.add(new Option(TownsSettings.questionerAccept(), new JoinNationTask(townMayor, nation)));
			options.add(new Option(TownsSettings.questionerDeny(), new ResidentNationQuestionTask(townMayor, nation) {
				@Override
				public void run() {
					TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_deny_invite"), getResident().getName()));
				}
			}));
			Question question = new Question(townMayor.getName(), String.format(TownsSettings.getLangString("msg_invited"), nation.getName()), options);
			try {
				plugin.appendQuestion(question);
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		} else {
			nation.addTown(town);
			plugin.updateCache();
			TownsUniverse.getDataSource().saveTown(town);
		}
	}

	public static void nationAdd(Nation nation, List<Town> towns) throws AlreadyRegisteredException {
		for (Town town : towns) {
			if (! town.hasNation()) {
				nation.addTown(town);
				plugin.updateCache();
				TownsUniverse.getDataSource().saveTown(town);
				TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_join_nation"), town.getName()));
			}
		}
		TownsUniverse.getDataSource().saveNation(nation);
	}

	public void nationKick(Player player, String[] names) {
		if (names.length < 1) {
			TownsMessaging.sendErrorMsg(player, "Eg: /nation kick [names]");
			return;
		}

		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				if (! nation.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
				}
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		nationKick(player, resident, nation, plugin.getTownsUniverse().getTowns(names));
	}

	public void nationKick(Player player, Resident resident, Nation nation, List<Town> kicking) {
		ArrayList<Town> remove = new ArrayList<Town>();
		for (Town town : kicking) {
			if (town.isCapital()) {
				remove.add(town);
			} else {
				try {
					nation.removeTown(town);
					plugin.updateCache();
					TownsUniverse.getDataSource().saveTown(town);
				} catch (NotRegisteredException e) {
					remove.add(town);
				} catch (EmptyNationException e) {
					// You can't kick yourself and only the mayor can kick assistants
					// so there will always be at least one resident.
				}
			}
		}

		for (Town town : remove) {
			kicking.remove(town);
		}

		if (kicking.size() > 0) {
			String msg = "";

			for (Town town : kicking) {
				msg += town.getName() + ", ";

				msg = msg.substring(0, msg.length() - 2);
				TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_nation_kicked_by"), player.getName()));
			}
			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_nation_kicked"), player.getName(), msg);
			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveNation(nation);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public void nationAssistant(Player player, String[] split) {
		if (split.length == 0) {
			//TODO: assistant help
		} else if (split[0].equalsIgnoreCase("add")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			nationAssistantsAdd(player, newSplit, true);
		} else if (split[0].equalsIgnoreCase("remove")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			nationAssistantsRemove(player, newSplit, true);
		} else if (split[0].equalsIgnoreCase("add+")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			nationAssistantsAdd(player, newSplit, false);
		} else if (split[0].equalsIgnoreCase("remove+")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			nationAssistantsRemove(player, newSplit, false);
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

	public void nationAssistantsAdd(Player player, String[] names, boolean matchOnline) {
		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				throw new TownsException(TownsSettings.getLangString("msg_not_king"));
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		nationAssistantsAdd(player, nation, (matchOnline ? plugin.getTownsUniverse().getOnlineResidents(player, names) : getResidents(player, names)));
	}

	public void nationAssistantsAdd(Player player, Nation nation, List<Resident> invited) {
		//TODO: change variable names from townAdd copypasta
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident newMember : invited) {
			try {
				nation.addAssistant(newMember);
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

			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_raised_ass"), player.getName(), msg, "nation");
			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveNation(nation);
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

	public void nationAssistantsRemove(Player player, String[] names, boolean matchOnline) {
		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				throw new TownsException(TownsSettings.getLangString("msg_not_king"));
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		nationAssistantsRemove(player, resident, nation, (matchOnline ? plugin.getTownsUniverse().getOnlineResidents(player, names) : getResidents(player, names)));
	}

	public void nationAssistantsRemove(Player player, Resident resident, Nation nation, List<Resident> kicking) {
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident member : kicking) {
			try {
				nation.removeAssistant(member);
				plugin.deleteCache(member.getName());
				TownsUniverse.getDataSource().saveResident(member);
				TownsUniverse.getDataSource().saveNation(nation);
			} catch (NotRegisteredException e) {
				remove.add(member);
			}
		}
		for (Resident member : remove) {
			kicking.remove(member);
		}

		if (kicking.size() > 0) {
			String msg = "";

			for (Resident member : kicking) {
				msg += member.getName() + ", ";
				/* removed to prevent multiple message spam.
				 * 
				Player p = plugin.getServer().getPlayer(member.getName());
				if (p != null)
						p.sendMessage(String.format(TownsSettings.getLangString("msg_lowered_to_res_by"), player.getName()));
				*/
			}
			msg = msg.substring(0, msg.length() - 2);
			msg = String.format(TownsSettings.getLangString("msg_lowered_to_res"), player.getName(), msg);
			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveNation(nation);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public void nationAlly(Player player, String[] split) {
		if (split.length < 2) {
			TownsMessaging.sendErrorMsg(player, "Eg: /nation ally [add/remove] [name]");
			return;
		}

		Resident resident;
		Nation nation;
		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				if (! nation.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
				}
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		ArrayList<Nation> list = new ArrayList<Nation>();
		Nation ally;
		//test add or remove
		String test = split[0];
		String[] newSplit = StringMgmt.remFirstArg(split);

		if ((test.equalsIgnoreCase("remove") || test.equalsIgnoreCase("add")) && newSplit.length > 0) {
			for (String name : newSplit) {
				try {
					ally = plugin.getTownsUniverse().getNation(name);
					if (nation.equals(ally)) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_own_nation_disallow"));
					} else {
						list.add(ally);
					}
				} catch (NotRegisteredException e) {
					// Do nothing here as the name doesn't match a Nation
				}
			}
			if (! list.isEmpty()) {
				nationAlly(player, nation, list, test.equalsIgnoreCase("add"));
			}
		} else {
			TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "[add/remove]"));
		}
	}

	public void nationAlly(Player player, Nation nation, List<Nation> allies, boolean add) {
		ArrayList<Nation> remove = new ArrayList<Nation>();
		for (Nation targetNation : allies) {
			try {
				if (add && ! nation.getAllies().contains(targetNation)) {
					nation.addAlly(targetNation);
					TownsMessaging.sendNationMessage(targetNation, String.format(TownsSettings.getLangString("msg_added_ally"), nation.getName()));
				} else if (nation.getAllies().contains(targetNation)) {
					nation.removeAlly(targetNation);
					TownsMessaging.sendNationMessage(targetNation, String.format(TownsSettings.getLangString("msg_removed_ally"), nation.getName()));
				}

				plugin.updateCache();
			} catch (AlreadyRegisteredException e) {
				remove.add(targetNation);
			} catch (NotRegisteredException e) {
				remove.add(targetNation);
			}
		}

		for (Nation newAlly : remove) {
			allies.remove(newAlly);
		}

		if (allies.size() > 0) {
			String msg = "";

			for (Nation newAlly : allies) {
				msg += newAlly.getName() + ", ";
			}

			msg = msg.substring(0, msg.length() - 2);
			if (add) {
				msg = String.format(TownsSettings.getLangString("msg_allied_nations"), player.getName(), msg);
			} else {
				msg = String.format(TownsSettings.getLangString("msg_broke_alliance"), player.getName(), msg);
			}

			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveNations();
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public void nationEnemy(Player player, String[] split) {
		Resident resident;
		Nation nation;

		if (split.length < 2) {
			TownsMessaging.sendErrorMsg(player, "Eg: /nation enemy [add/remove] [name]");
			return;
		}

		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
			nation = resident.getTown().getNation();
			if (! resident.isKing()) {
				if (! nation.hasAssistant(resident)) {
					throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
				}
			}
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
			return;
		}

		ArrayList<Nation> list = new ArrayList<Nation>();
		Nation enemy;
		//test add or remove
		String test = split[0];
		String[] newSplit = StringMgmt.remFirstArg(split);

		if ((test.equalsIgnoreCase("remove") || test.equalsIgnoreCase("add")) && newSplit.length > 0) {
			for (String name : newSplit) {
				try {
					enemy = plugin.getTownsUniverse().getNation(name);
					if (nation.equals(enemy)) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_own_nation_disallow"));
					} else {
						list.add(enemy);
					}
				} catch (NotRegisteredException e) {
					// Do nothing here as the name doesn't match a Nation
				}
			}
			if (! list.isEmpty()) {
				nationEnemy(player, nation, list, test.equalsIgnoreCase("add"));
			}
		} else {
			TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "[add/remove]"));
		}
	}

	public void nationEnemy(Player player, Nation nation, List<Nation> enemies, boolean add) {
		ArrayList<Nation> remove = new ArrayList<Nation>();
		for (Nation targetNation : enemies) {
			try {
				if (add && ! nation.getEnemies().contains(targetNation)) {
					nation.addEnemy(targetNation);
					TownsMessaging.sendNationMessage(targetNation, String.format(TownsSettings.getLangString("msg_added_enemy"), nation.getName()));
				} else if (nation.getEnemies().contains(targetNation)) {
					nation.removeEnemy(targetNation);
					TownsMessaging.sendNationMessage(targetNation, String.format(TownsSettings.getLangString("msg_removed_enemy"), nation.getName()));
				}

				plugin.updateCache();
			} catch (AlreadyRegisteredException e) {
				remove.add(targetNation);
			} catch (NotRegisteredException e) {
				remove.add(targetNation);
			}
		}

		for (Nation newEnemy : remove) {
			enemies.remove(newEnemy);
		}

		if (enemies.size() > 0) {
			String msg = "";

			for (Nation newEnemy : enemies) {
				msg += newEnemy.getName() + ", ";
			}

			msg = msg.substring(0, msg.length() - 2);
			if (add) {
				msg = String.format(TownsSettings.getLangString("msg_enemy_nations"), player.getName(), msg);
			} else {
				msg = String.format(TownsSettings.getLangString("msg_enemy_to_neutral"), player.getName(), msg);
			}

			TownsMessaging.sendNationMessage(nation, ChatTools.color(msg));
			TownsUniverse.getDataSource().saveNations();
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public void nationSet(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/nation set"));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "king " + TownsSettings.getLangString("res_2"), ""));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "capital [town]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "taxes [$]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "name [name]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "title/surname [resident] [text]", ""));
			player.sendMessage(ChatTools.formatCommand("", "/nation set", "tag [upto 4 letters] or clear", ""));
		} else {
			Resident resident;
			Nation nation;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				nation = resident.getTown().getNation();
				if (! resident.isKing()) {
					if (! nation.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			// TODO: Let admin's call a subfunction of this.
			if (split[0].equalsIgnoreCase("king")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set king Dumbo");
				} else {
					try {
						Resident newKing = plugin.getTownsUniverse().getResident(split[1]);
						String oldKingsName = nation.getCapital().getMayor().getName();
						nation.setKing(newKing);
						plugin.deleteCache(oldKingsName);
						plugin.deleteCache(newKing.getName());
						TownsMessaging.sendNationMessage(nation, TownsSettings.getNewKingMsg(newKing.getName(), nation.getName()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getError());
					}
				}
			} else if (split[0].equalsIgnoreCase("capital")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set capital {town name}");
				} else {
					try {
						Town newCapital = plugin.getTownsUniverse().getTown(split[1]);
						nation.setCapital(newCapital);
						plugin.updateCache();
						TownsMessaging.sendNationMessage(nation, TownsSettings.getNewKingMsg(newCapital.getMayor().getName(), nation.getName()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getError());
					}
				}
			} else if (split[0].equalsIgnoreCase("taxes")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set taxes 70");
				} else {
					Integer amount = Integer.parseInt(split[1].trim());
					if (amount < 0) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_negative_money"));
						return;
					}

					try {
						nation.setTaxes(amount);
						TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_town_set_nation_tax"), player.getName(), split[1]));
					} catch (NumberFormatException e) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_error_must_be_int"));
					}
				}
			} else if (split[0].equalsIgnoreCase("name")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set name Plutoria");
				} else {
					if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.nation.rename"))) {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
						return;
					}

					if (TownsSettings.isValidRegionName(split[1])) {
						nationRename(player, nation, split[1]);
					} else {
						TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
					}
				}
			} else if (split[0].equalsIgnoreCase("tag")) {
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set tag PLT");
				} else if (split[1].equalsIgnoreCase("clear")) {
					try {
						nation.setTag(" ");
						TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_reset_nation_tag"), player.getName()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					}
				} else {
					try {
						nation.setTag(plugin.getTownsUniverse().checkAndFilterName(split[1]));
						TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_set_nation_tag"), player.getName(), nation.getTag()));
					} catch (TownsException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					} catch (InvalidNameException e) {
						TownsMessaging.sendErrorMsg(player, e.getMessage());
					}
				}
			} else if (split[0].equalsIgnoreCase("title")) {
				// Give the resident a title
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set title bilbo Jester ");
				} else {
					try {
						if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.nation.titles"))) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
							return;
						}

						resident = plugin.getTownsUniverse().getResident(split[1]);
						if (resident.hasNation()) {
							if (resident.getTown().getNation() != plugin.getTownsUniverse().getResident(player.getName()).getTown().getNation()) {
								TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_same_nation"), resident.getName()));
								return;
							}
						} else {
							TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_same_nation"), resident.getName()));
							return;
						}
						split = StringMgmt.remArgs(split, 2);
						if (StringMgmt.join(split).length() > TownsSettings.getMaxTitleLength()) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_input_too_long"));
							return;
						}

						String title = StringMgmt.join(plugin.getTownsUniverse().checkAndFilterArray(split));
						resident.setTitle(title + " ");
						TownsUniverse.getDataSource().saveResident(resident);

						if (resident.hasTitle()) {
							TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_set_title"), resident.getName(), resident.getTitle()));
						} else {
							TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_clear_title_surname"), "Title", resident.getName()));
						}
					} catch (NotRegisteredException e) {
						TownsMessaging.sendErrorMsg(player, e.getError());
					}
				}
			} else if (split[0].equalsIgnoreCase("surname")) {
				// Give the resident a title
				if (split.length < 2) {
					TownsMessaging.sendErrorMsg(player, "Eg: /nation set surname bilbo the dwarf ");
				} else {
					try {
						if (plugin.isPermissions() && (! TownsUniverse.getPermissionSource().hasPermission(player, "towns.nation.titles"))) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
							return;
						}

						resident = plugin.getTownsUniverse().getResident(split[1]);
						if (resident.hasNation()) {
							if (resident.getTown().getNation() != plugin.getTownsUniverse().getResident(player.getName()).getTown().getNation()) {
								TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_same_nation"), resident.getName()));
								return;
							}
						} else {
							TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_same_nation"), resident.getName()));
							return;
						}
						split = StringMgmt.remArgs(split, 2);
						if (StringMgmt.join(split).length() > TownsSettings.getMaxTitleLength()) {
							TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_input_too_long"));
							return;
						}

						String surname = StringMgmt.join(plugin.getTownsUniverse().checkAndFilterArray(split));
						resident.setSurname(" " + surname);
						TownsUniverse.getDataSource().saveResident(resident);

						if (resident.hasSurname()) {
							TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_set_surname"), resident.getName(), resident.getSurname()));
						} else {
							TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_clear_title_surname"), "Surname", resident.getName()));
						}
					} catch (NotRegisteredException e) {
						TownsMessaging.sendErrorMsg(player, e.getError());
					}
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "nation"));
				return;
			}

			TownsUniverse.getDataSource().saveNation(nation);
		}
	}

	public void nationToggle(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatTitle("/nation toggle"));
			player.sendMessage(ChatTools.formatCommand("", "/nation toggle", "neutral", ""));
		} else {
			Resident resident;
			Nation nation;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
				nation = resident.getTown().getNation();
				if (! resident.isKing()) {
					if (! nation.hasAssistant(resident)) {
						throw new TownsException(TownsSettings.getLangString("msg_not_king_ass"));
					}
				}
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			if (split[0].equalsIgnoreCase("neutral")) {
				try {
					if (! TownsSettings.isDeclaringNeutral()) {
						throw new TownsException(TownsSettings.getLangString("msg_neutral_disabled"));
					}

					boolean choice = ! nation.isNeutral();
					Double cost = TownsSettings.getNationNeutralityCost();

					if (choice && TownsSettings.isUsingEconomy() && ! nation.pay(cost, "Nation Neutrality Cost")) {
						throw new TownsException(TownsSettings.getLangString("msg_nation_cant_neutral"));
					}

					nation.setNeutral(choice);
					plugin.updateCache();

					// send message depending on if using IConomy and charging for neutral
					if (TownsSettings.isUsingEconomy() && cost > 0) {
						TownsMessaging.sendMsg(player, String.format(TownsSettings.getLangString("msg_you_paid"), cost + TownsEconomyObject.getEconomyCurrency()));
					} else {
						TownsMessaging.sendMsg(player, TownsSettings.getLangString("msg_nation_set_neutral"));
					}

					TownsMessaging.sendNationMessage(nation, TownsSettings.getLangString("msg_nation_neutral") + (nation.isNeutral() ? Colors.Green : Colors.Red + " not") + " neutral.");
				} catch (TownsException e) {
					try {
						nation.setNeutral(false);
					} catch (TownsException e1) {
						e1.printStackTrace();
					}
					TownsMessaging.sendErrorMsg(player, e.getError());
				} catch (Exception e) {
					TownsMessaging.sendErrorMsg(player, e.getMessage());
				}
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "nation"));
				return;
			}

			plugin.getTownsUniverse();
			TownsUniverse.getDataSource().saveNation(nation);
		}
	}

	public void nationRename(Player player, Nation nation, String newName) {
		try {
			plugin.getTownsUniverse().renameNation(nation, newName);
			TownsMessaging.sendNationMessage(nation, String.format(TownsSettings.getLangString("msg_nation_set_name"), player.getName(), nation.getName()));
		} catch (TownsException e) {
			TownsMessaging.sendErrorMsg(player, e.getError());
		}
	}

	private List<Resident> getResidents(Player player, String[] names) {
		List<Resident> invited = new ArrayList<Resident>();
		for (String name : names) {
			try {
				Resident target = plugin.getTownsUniverse().getResident(name);
				invited.add(target);
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
			}
		}
		return invited;
	}
}
