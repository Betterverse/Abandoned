package net.betterverse.towns;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;

/**
 * Towns message handling class
 *
 * @author ElgarL
 */

public class TownsMessaging {
	/**
	 * Sends an error message to the log
	 *
	 * @param msg
	 */
	public static void sendErrorMsg(String msg) {
		TownsLogger.log.warning("[Towns] Error: " + msg);
	}

	/**
	 * Sends an Error message (red) to the Player or console
	 * and to the named Dev if DevMode is enabled.
	 * Uses default_towns_prefix
	 *
	 * @param sender
	 * @param msg
	 */
	public static void sendErrorMsg(Object sender, String msg) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}

		for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + Colors.Rose + msg)) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(Colors.strip(line));
			}
		}
		sendDevMsg(msg);
	}

	/**
	 * Sends an Error message (red) to the Player or console
	 * and to the named Dev if DevMode is enabled.
	 * Uses default_towns_prefix
	 *
	 * @param sender
	 * @param msg
	 */
	public static void sendErrorMsg(Object sender, String[] msg) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}

		for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + Colors.Rose + msg)) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(Colors.strip(line));
			}
		}
		sendDevMsg(msg);
	}

	/**
	 * Sends a message to console only
	 * prefixed by [Towns]
	 *
	 * @param msg
	 */
	public static void sendMsg(String msg) {
		System.out.println("[Towns] " + ChatTools.stripColour(msg));
	}

	/**
	 * Sends a message (green) to the Player or console
	 * and to the named Dev if DevMode is enabled.
	 * Uses default_towns_prefix
	 *
	 * @param sender
	 * @param msg
	 */
	public static void sendMsg(Object sender, String msg) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}

		for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + Colors.Green + msg)) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(Colors.strip(line));
			}
		}
		sendDevMsg(msg);
	}

	/**
	 * Sends a message (green) to the Player or console
	 * and to the named Dev if DevMode is enabled.
	 * Uses default_towns_prefix
	 *
	 * @param sender
	 * @param msg
	 */
	public static void sendMsg(Player player, String[] msg) {
		for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + Colors.Green + msg)) {
			player.sendMessage(line);
		}
	}

	/**
	 * Sends a message (red) to the named Dev (if DevMode is enabled)
	 * Uses default_towns_prefix
	 *
	 * @param msg
	 */
	public static void sendDevMsg(String msg) {
		if (TownsSettings.isDevMode()) {
			Player townsDev = TownsUniverse.plugin.getServer().getPlayer(TownsSettings.getDevName());
			if (townsDev == null) {
				return;
			}
			for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + " DevMode: " + Colors.Rose + msg)) {
				townsDev.sendMessage(line);
			}
		}
	}

	/**
	 * Sends a message (red) to the named Dev (if DevMode is enabled)
	 * Uses default_towns_prefix
	 *
	 * @param msg
	 */
	public static void sendDevMsg(String[] msg) {
		if (TownsSettings.isDevMode()) {
			Player townsDev = TownsUniverse.plugin.getServer().getPlayer(TownsSettings.getDevName());
			if (townsDev == null) {
				return;
			}
			for (String line : ChatTools.color(TownsSettings.getLangString("default_towns_prefix") + " DevMode: " + Colors.Rose + msg)) {
				townsDev.sendMessage(line);
			}
		}
	}

	/**
	 * Sends a message to the log and console
	 * prefixed by [Towns] Debug:
	 *
	 * @param msg
	 */
	public static void sendDebugMsg(String msg) {
		if (TownsSettings.getDebug()) {
			TownsLogger.debug.info("[Towns] Debug: " + msg);
		}
		sendDevMsg(msg);
	}

	/////////////////

	/**
	 * Send a message to a player
	 *
	 * @param player
	 * @param lines
	 */
	public static void sendMessage(Object sender, List<String> lines) {
		sendMessage(sender, lines.toArray(new String[0]));
	}

	/**
	 * Send a message to a player
	 *
	 * @param player
	 * @param line
	 */
	public static void sendMessage(Object sender, String line) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}

		if (isPlayer) {
			((Player) sender).sendMessage(line);
		} else {
			((CommandSender) sender).sendMessage(line);
		}
	}

	/**
	 * Send a message to a player
	 *
	 * @param player
	 * @param lines
	 */
	public static void sendMessage(Object sender, String[] lines) {
		boolean isPlayer = false;
		if (sender instanceof Player) {
			isPlayer = true;
		}

		for (String line : lines) {
			if (isPlayer) {
				((Player) sender).sendMessage(line);
			} else {
				((CommandSender) sender).sendMessage(line);
			}
		}
	}

	/**
	 * Send a message to all online residents of a town
	 *
	 * @param town
	 * @param lines
	 */
	public static void sendTownMessage(Town town, List<String> lines) {
		sendTownMessage(town, lines.toArray(new String[0]));
	}

	/**
	 * Send a message to all online residents of a nation
	 *
	 * @param nation
	 * @param lines
	 */
	public static void sendNationMessage(Nation nation, List<String> lines) {
		sendNationMessage(nation, lines.toArray(new String[0]));
	}

	/**
	 * Send a message to ALL online players and the log.
	 *
	 * @param lines
	 */
	public static void sendGlobalMessage(List<String> lines) {
		sendGlobalMessage(lines.toArray(new String[0]));
	}

	/**
	 * Send a message to ALL online players and the log.
	 *
	 * @param lines
	 */
	public static void sendGlobalMessage(String[] lines) {
		for (String line : lines) {
			TownsUniverse.plugin.log("[Global Msg] " + line);
		}
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers()) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	/**
	 * Send a message to All online players and the log.
	 *
	 * @param line
	 */
	public static void sendGlobalMessage(String line) {
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers()) {
			player.sendMessage(line);
			TownsUniverse.plugin.log("[Global Message] " + player.getName() + ": " + line);
		}
	}

	/**
	 * Send a message to a specific resident
	 *
	 * @param resident
	 * @param lines
	 * @throws TownsException
	 */
	public static void sendResidentMessage(Resident resident, String[] lines) throws TownsException {
		for (String line : lines) {
			TownsUniverse.plugin.log("[Resident Msg] " + resident.getName() + ": " + line);
		}
		Player player = TownsUniverse.plugin.getTownsUniverse().getPlayer(resident);
		for (String line : lines) {
			player.sendMessage(line);
		}
	}

	/**
	 * Send a message to a specific resident
	 *
	 * @param resident
	 * @param lines
	 * @throws TownsException
	 */
	public static void sendResidentMessage(Resident resident, String line) throws TownsException {
		TownsUniverse.plugin.log("[Resident Msg] " + resident.getName() + ": " + line);
		Player player = TownsUniverse.plugin.getTownsUniverse().getPlayer(resident);
		player.sendMessage(TownsSettings.getLangString("default_towns_prefix") + line);
	}

	/**
	 * Send a message to All online residents of a town and log
	 *
	 * @param town
	 * @param lines
	 */
	public static void sendTownMessage(Town town, String[] lines) {
		for (String line : lines) {
			TownsUniverse.plugin.log("[Town Msg] " + town.getName() + ": " + line);
		}
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(town)) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	/**
	 * Send a message to All online residents of a town and log
	 *
	 * @param town
	 * @param lines
	 */
	public static void sendTownMessagePrefixed(Town town, String line) {
		TownsUniverse.plugin.log("[Town Msg] " + town.getName() + ": " + line);
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(town)) {
			player.sendMessage(TownsSettings.getLangString("default_towns_prefix") + line);
		}
	}

	/**
	 * Send a message to All online residents of a town and log
	 *
	 * @param town
	 * @param lines
	 */
	public static void sendTownMessage(Town town, String line) {
		TownsUniverse.plugin.log("[Town Msg] " + town.getName() + ": " + line);
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(town)) {
			player.sendMessage(line);
		}
	}

	/**
	 * Send a message to All online residents of a nation and log
	 *
	 * @param nation
	 * @param lines
	 */
	public static void sendNationMessage(Nation nation, String[] lines) {
		for (String line : lines) {
			TownsUniverse.plugin.log("[Nation Msg] " + nation.getName() + ": " + line);
		}
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(nation)) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	/**
	 * Send a message to All online residents of a nation and log
	 *
	 * @param nation
	 * @param lines
	 */
	public static void sendNationMessage(Nation nation, String line) {
		TownsUniverse.plugin.log("[Nation Msg] " + nation.getName() + ": " + line);
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(nation)) {
			player.sendMessage(line);
		}
	}

	/**
	 * Send a message to All online residents of a nation and log
	 *
	 * @param nation
	 * @param lines
	 */
	public static void sendNationMessagePrefixed(Nation nation, String line) {
		TownsUniverse.plugin.log("[Nation Msg] " + nation.getName() + ": " + line);
		for (Player player : TownsUniverse.plugin.getTownsUniverse().getOnlinePlayers(nation)) {
			player.sendMessage(TownsSettings.getLangString("default_towns_prefix") + line);
		}
	}

	/**
	 * Send the town board to a player (in yellow)
	 *
	 * @param player
	 * @param town
	 */
	public static void sendTownBoard(Player player, Town town) {
		for (String line : ChatTools.color(Colors.Gold + "[" + town.getName() + "] " + Colors.Yellow + town.getTownBoard())) {
			player.sendMessage(line);
		}
	}
}