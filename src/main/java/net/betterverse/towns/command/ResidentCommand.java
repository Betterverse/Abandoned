package net.betterverse.towns.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.StringMgmt;
import net.betterverse.towns.war.TownsWarConfig;

/**
 * Send a list of all towns resident help commands to player Command: /resident
 */
public class ResidentCommand implements CommandExecutor {
	private static Towns plugin;
	private static final List<String> output = new ArrayList<String>();
	static {
		output.add(ChatTools.formatTitle("/resident"));
		output.add(ChatTools.formatCommand("", "/resident", "", TownsSettings.getLangString("res_1")));
		output.add(ChatTools.formatCommand("", "/resident", TownsSettings.getLangString("res_2"), TownsSettings.getLangString("res_3")));
		output.add(ChatTools.formatCommand("", "/resident", "list", TownsSettings.getLangString("res_4")));
		output.add(ChatTools.formatCommand("", "/resident", "set [] .. []", "'/resident set' " + TownsSettings.getLangString("res_5")));
		output.add(ChatTools.formatCommand("", "/resident", "friend [add/remove] " + TownsSettings.getLangString("res_2"), TownsSettings.getLangString("res_6")));
		output.add(ChatTools.formatCommand("", "/resident", "friend [add+/remove+] " + TownsSettings.getLangString("res_2") + " ", TownsSettings.getLangString("res_7")));
		output.add(ChatTools.formatCommand(TownsSettings.getLangString("admin_sing"), "/resident", "delete " + TownsSettings.getLangString("res_2"), ""));
	}

	public ResidentCommand(Towns instance) {
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
				parseResidentCommand(player, args);
			} else {
				parseResidentCommand(player, args);
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

	public void parseResidentCommand(Player player, String[] split) {
		if (split.length == 0) {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(player.getName());
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(resident));
			} catch (NotRegisteredException x) {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_not_registered"));
			}
		} else if (split[0].equalsIgnoreCase("?") || split[0].equalsIgnoreCase("help")) {
			for (String line : output) {
				player.sendMessage(line);
			}
		} else if (split[0].equalsIgnoreCase("list")) {
			listResidents(player);
		} else if (split[0].equalsIgnoreCase("set")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			residentSet(player, newSplit);
		} else if (split[0].equalsIgnoreCase("friend")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			residentFriend(player, newSplit);
		} else if (split[0].equalsIgnoreCase("delete")) {
			String[] newSplit = StringMgmt.remFirstArg(split);
			residentDelete(player, newSplit);
		} else {
			try {
				Resident resident = plugin.getTownsUniverse().getResident(split[0]);
				TownsMessaging.sendMessage(player, plugin.getTownsUniverse().getStatus(resident));
			} catch (NotRegisteredException x) {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_not_registered_1"), split[0]));
			}
		}
	}

	public void listResidents(Player player) {
		player.sendMessage(ChatTools.formatTitle(TownsSettings.getLangString("res_list")));
		String colour;
		ArrayList<String> formatedList = new ArrayList<String>();
		for (Resident resident : plugin.getTownsUniverse().getActiveResidents()) {
			if (resident.isKing()) {
				colour = Colors.Gold;
			} else if (resident.isMayor()) {
				colour = Colors.LightBlue;
			} else {
				colour = Colors.White;
			}
			formatedList.add(colour + resident.getName() + Colors.White);
		}
		for (String line : ChatTools.list(formatedList)) {
			player.sendMessage(line);
		}
	}

	/**
	 * Command: /resident set [] ... []
	 *
	 * @param player
	 * @param split
	 */

	/*
	 * perm [resident/outsider] [build/destroy] [on/off]
	 */
	public void residentSet(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatCommand("", "/resident set", "perm ...", "'/resident set perm' " + TownsSettings.getLangString("res_5")));
			player.sendMessage(ChatTools.formatCommand("", "/resident set", "mode ...", "'/resident set mode' " + TownsSettings.getLangString("res_5")));
		} else {
			Resident resident;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			// TODO: Let admin's call a subfunction of this.
			if (split[0].equalsIgnoreCase("perm")) {
				String[] newSplit = StringMgmt.remFirstArg(split);
				TownCommand.setTownBlockPermissions(player, resident, resident.getPermissions(), newSplit, true);
			} else if (split[0].equalsIgnoreCase("mode")) {
				String[] newSplit = StringMgmt.remFirstArg(split);
				setMode(player, newSplit);
			} else {
				TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_property"), "town"));
				return;
			}

			TownsUniverse.getDataSource().saveResident(resident);
		}
	}

	private void setMode(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatCommand("", "/resident set mode", "clear", ""));
			player.sendMessage(ChatTools.formatCommand("", "/resident set mode", "[mode] ...[mode]", ""));
			player.sendMessage(ChatTools.formatCommand("Mode", "map", "", TownsSettings.getLangString("mode_1")));
			player.sendMessage(ChatTools.formatCommand("Mode", "townclaim", "", TownsSettings.getLangString("mode_2")));
			player.sendMessage(ChatTools.formatCommand("Mode", "townunclaim", "", TownsSettings.getLangString("mode_3")));
			player.sendMessage(ChatTools.formatCommand("Mode", "tc", "", TownsSettings.getLangString("mode_4")));
			player.sendMessage(ChatTools.formatCommand("Mode", "nc", "", TownsSettings.getLangString("mode_5")));
			String warFlagMaterial = (TownsWarConfig.getFlagBaseMaterial() == null ? "flag" : TownsWarConfig.getFlagBaseMaterial().name().toLowerCase());
			player.sendMessage(ChatTools.formatCommand("Mode", "warflag", "", String.format(TownsSettings.getLangString("mode_6"), warFlagMaterial)));
			player.sendMessage(ChatTools.formatCommand("Eg", "/resident set mode", "map townclaim tc nc", ""));
		} else if (split[0].equalsIgnoreCase("reset") || split[0].equalsIgnoreCase("clear")) {
			plugin.removePlayerMode(player);
		} else {
			plugin.setPlayerMode(player, split);
		}
	}

	public void residentFriend(Player player, String[] split) {
		if (split.length == 0) {
			player.sendMessage(ChatTools.formatCommand("", "/resident friend", "add " + TownsSettings.getLangString("res_2"), ""));
			player.sendMessage(ChatTools.formatCommand("", "/resident friend", "remove " + TownsSettings.getLangString("res_2"), ""));
			player.sendMessage(ChatTools.formatCommand("", "/resident friend", "clear", ""));
		} else {
			Resident resident;
			try {
				resident = plugin.getTownsUniverse().getResident(player.getName());
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}

			// TODO: Let admin's call a subfunction of this.
			if (split[0].equalsIgnoreCase("add")) {
				String[] names = StringMgmt.remFirstArg(split);
				residentFriendAdd(player, resident, plugin.getTownsUniverse().getOnlineResidents(player, names));
			} else if (split[0].equalsIgnoreCase("remove")) {
				String[] names = StringMgmt.remFirstArg(split);
				residentFriendRemove(player, resident, plugin.getTownsUniverse().getOnlineResidents(player, names));
			} else if (split[0].equalsIgnoreCase("add+")) {
				String[] names = StringMgmt.remFirstArg(split);
				residentFriendAdd(player, resident, getResidents(player, names));
			} else if (split[0].equalsIgnoreCase("remove+")) {
				String[] names = StringMgmt.remFirstArg(split);
				residentFriendRemove(player, resident, getResidents(player, names));
			} else if (split[0].equalsIgnoreCase("clearlist") || split[0].equalsIgnoreCase("clear")) {
				residentFriendRemove(player, resident, resident.getFriends());
			}
		}
	}

	private static List<Resident> getResidents(Player player, String[] names) {
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

	public void residentFriendAdd(Player player, Resident resident, List<Resident> invited) {
		ArrayList<Resident> remove = new ArrayList<Resident>();
		for (Resident newFriend : invited) {
			try {
				resident.addFriend(newFriend);
				plugin.deleteCache(newFriend.getName());
			} catch (AlreadyRegisteredException e) {
				remove.add(newFriend);
			}
		}
		for (Resident newFriend : remove) {
			invited.remove(newFriend);
		}

		if (invited.size() > 0) {
			String msg = "Added ";
			for (Resident newFriend : invited) {
				msg += newFriend.getName() + ", ";
				Player p = plugin.getServer().getPlayer(newFriend.getName());
				if (p != null) {
					TownsMessaging.sendMsg(p, String.format(TownsSettings.getLangString("msg_friend_add"), player.getName()));
				}
			}
			msg = msg.substring(0, msg.length() - 2);
			msg += TownsSettings.getLangString("msg_to_list");
			TownsMessaging.sendMsg(player, msg);
			TownsUniverse.getDataSource().saveResident(resident);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	public void residentFriendRemove(Player player, Resident resident, List<Resident> kicking) {
		List<Resident> remove = new ArrayList<Resident>();
		List<Resident> toKick = new ArrayList<Resident>(kicking);

		for (Resident friend : toKick) {
			try {
				resident.removeFriend(friend);
				plugin.deleteCache(friend.getName());
			} catch (NotRegisteredException e) {
				remove.add(friend);
			}
		}
		// remove invalid names so we don't try to send them messages				   
		if (remove.size() > 0) {
			for (Resident friend : remove) {
				toKick.remove(friend);
			}
		}

		if (toKick.size() > 0) {
			String msg = TownsSettings.getLangString("msg_removed");
			Player p;
			for (Resident member : toKick) {
				msg += member.getName() + ", ";
				p = plugin.getServer().getPlayer(member.getName());
				if (p != null) {
					TownsMessaging.sendMsg(p, String.format(TownsSettings.getLangString("msg_friend_remove"), player.getName()));
				}
			}
			msg = msg.substring(0, msg.length() - 2);
			msg += TownsSettings.getLangString("msg_from_list");
			;
			TownsMessaging.sendMsg(player, msg);
			TownsUniverse.getDataSource().saveResident(resident);
		} else {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		}
	}

	/**
	 * Delete a resident and it's data file (if not online)
	 * Available Only to players with the 'towns.admin' permission node.
	 *
	 * @param player
	 * @param split
	 */
	public void residentDelete(Player player, String[] split) {
		if (split.length == 0) {
			TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_invalid_name"));
		} else {
			try {
				if (! plugin.isTownsAdmin(player)) {
					throw new TownsException(TownsSettings.getLangString("msg_err_admin_only_delete"));
				}

				for (String name : split) {
					try {
						Resident resident = plugin.getTownsUniverse().getResident(name);
						if (! resident.isNPC() && ! plugin.isOnline(resident.getName())) {
							plugin.getTownsUniverse().removeResident(resident);
							plugin.getTownsUniverse().removeResidentList(resident);
							TownsMessaging.sendGlobalMessage(TownsSettings.getDelResidentMsg(resident));
						} else {
							TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_online_or_npc"), name));
						}
					} catch (NotRegisteredException x) {
						// This name isn't registered as a resident
						TownsMessaging.sendErrorMsg(player, String.format(TownsSettings.getLangString("msg_err_invalid_name"), name));
					}
				}
			} catch (TownsException x) {
				// Admin only escape
				TownsMessaging.sendErrorMsg(player, x.getError());
				return;
			}
		}
	}
}
