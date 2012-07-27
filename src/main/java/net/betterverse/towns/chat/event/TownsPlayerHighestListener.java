package net.betterverse.towns.chat.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.chat.CraftIRCHandler;
import net.betterverse.towns.chat.TownsChatFormatter;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.StringMgmt;

public class TownsPlayerHighestListener implements Listener {
	private final Towns plugin;
	private CraftIRCHandler ircHander;

	public TownsPlayerHighestListener(Towns instance, CraftIRCHandler irc) {
		this.plugin = instance;
		this.ircHander = irc;
	}

	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();

		// Test if this player is registered with Towns.
		try {
			plugin.getTownsUniverse().getResident(player.getName());
		} catch (NotRegisteredException e) {
			return;
		}
		String split[] = event.getMessage().split("\\ ");
		String command = split[0].trim().toLowerCase();
		String message = "";

		if (split.length > 1) {
			message = StringMgmt.join(StringMgmt.remFirstArg(split), " ");
		}

		if (TownsSettings.chatChannelExists(command)) {
			event.setMessage(message);

			if (! plugin.isPermissions() || (plugin.isPermissions() && TownsUniverse.getPermissionSource().hasPermission(player, TownsSettings.getChatChannelPermission(command)))) {

				// Deal with special cases
				if (command.equalsIgnoreCase("/tc")) {
					if (message.isEmpty()) {
						//plugin.setPlayerChatMode(player, "tc");
					} else {
						// Town Chat
						parseTownChatCommand(event, command, player);
					}
					event.setCancelled(true);
				} else if (command.equalsIgnoreCase("/nc")) {
					if (message.isEmpty()) {
						//plugin.setPlayerChatMode(player, "nc");
					} else {
						// Nation Chat
						parseNationChatCommand(event, command, player);
					}
					event.setCancelled(true);
				} else if (command.equalsIgnoreCase("/g")) {
					if (message.isEmpty()) {
						//plugin.setPlayerChatMode(player, "g");
					} else {
						// Global Chat
						parseGlobalChannelChatCommand(event, command, player);
					}
					event.setCancelled(true);
				} else {
					if (message.isEmpty()) {
						//plugin.setPlayerChatMode(player, command.replace("/", ""));
					} else {
						// Custom channel Chat
						parseDefaultChannelChatCommand(event, command, player);
					}
					event.setCancelled(true);
				}
			} else {
				TownsMessaging.sendErrorMsg(player, TownsSettings.getLangString("msg_err_command_disable"));
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		Resident resident;

		try {
			resident = plugin.getTownsUniverse().getResident(player.getName());
		} catch (NotRegisteredException e) {
			return;
		}

		if (plugin.hasPlayerMode(player, "tc")) {
			// Town Chat
			parseTownChatCommand(event, "/tc", player);
		} else if (plugin.hasPlayerMode(player, "nc")) {
			// Nation chat
			parseNationChatCommand(event, "/nc", player);
		} else if (plugin.hasPlayerMode(player, "g")) {
			// Global chat
			//parseGlobalChannelChatCommand(event, "/g", player);

			// This is Global chat.
			if (TownsSettings.isUsingModifyChat()) {
				/*
			 try {
				 event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatGlobalChannelFormat());
			 } catch (NotRegisteredException e) {
				 // World not registered with Towns
				 e.printStackTrace();
			 }
						 */
			}

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));

			if (ircHander != null) {
				parseGlobalChannelChatCommand(event, "/g", player);
			} else {
				return;
			}
		} else {
			/*
			for (String channel : TownsSettings.getChatChannels()) {
				if (plugin.hasPlayerMode(player, channel.replace("/", ""))) {
					// Custom channel Chat
					parseDefaultChannelChatCommand(event, channel, player);
					event.setCancelled(true);
					return;
				}
			}
						*/

			// All chat modes are disabled, or this is Global chat.
			if (TownsSettings.isUsingModifyChat()) {
				/*
			 try {
				 event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatGlobalChannelFormat());
			 } catch (NotRegisteredException e) {
				 // World not registered with Towns
				 e.printStackTrace();
			 }
						 */
			}

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));

			if (ircHander != null) {
				parseGlobalChannelChatCommand(event, "/g", player);
			} else {
				return;
			}
		}
		event.setCancelled(true);
	}

	private void parseTownChatCommand(PlayerChatEvent event, String command, Player player) {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());
			Town town = resident.getTown();

			//event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatTownChannelFormat().replace("{channelTag}", TownsSettings.getChatChannelName(command)).replace("{msgcolour}", TownsSettings.getChatChannelColour(command)));

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));
			String msg = chatEvent.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());

			// Relay to IRC
			if (ircHander != null) {
				ircHander.IRCSender(msg);
			}

			TownsMessaging.sendTownMessage(town, msg);
		} catch (NotRegisteredException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}

	private void parseNationChatCommand(PlayerChatEvent event, String command, Player player) {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());
			Nation nation = resident.getTown().getNation();

			//event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatNationChannelFormat().replace("{channelTag}", TownsSettings.getChatChannelName(command)).replace("{msgcolour}", TownsSettings.getChatChannelColour(command)));

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));
			String msg = chatEvent.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());

			// Relay to IRC
			if (ircHander != null) {
				ircHander.IRCSender(msg);
			}

			TownsMessaging.sendNationMessage(nation, msg);
		} catch (NotRegisteredException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}

	private void parseDefaultChannelChatCommand(PlayerChatEvent event, String command, Player player) {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());

			//event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatDefaultChannelFormat().replace("{channelTag}", TownsSettings.getChatChannelName(command)).replace("{msgcolour}", TownsSettings.getChatChannelColour(command)));

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));
			String msg = chatEvent.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());

			TownsUniverse.plugin.log(msg);
			// Relay to IRC
			if (ircHander != null) {
				ircHander.IRCSender(msg);
			}

			for (Player test : plugin.getTownsUniverse().getOnlinePlayers()) {
				if (! plugin.isPermissions() || (plugin.isPermissions() && TownsUniverse.getPermissionSource().hasPermission(test, TownsSettings.getChatChannelPermission(command)))) {
					TownsMessaging.sendMessage(test, msg);
				}
			}
		} catch (NotRegisteredException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}

	private void parseGlobalChannelChatCommand(PlayerChatEvent event, String command, Player player) {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());

			if (TownsSettings.isUsingModifyChat()) {
				//event.setFormat(TownsUniverse.getWorld(player.getWorld().getName()).getChatGlobalChannelFormat());
			}

			TownsChatEvent chatEvent = new TownsChatEvent(event, resident);
			event.setFormat(TownsChatFormatter.getChatFormat(chatEvent));
			String msg = chatEvent.getFormat().replace("%1$s", event.getPlayer().getDisplayName()).replace("%2$s", event.getMessage());

			TownsUniverse.plugin.log(msg);
			// Relay to IRC
			if (ircHander != null) {
				ircHander.IRCSender(msg);
			}

			for (Player test : plugin.getTownsUniverse().getOnlinePlayers()) {
				if (! plugin.isPermissions() || (plugin.isPermissions() && TownsUniverse.getPermissionSource().hasPermission(test, TownsSettings.getChatChannelPermission(command)))) {
					TownsMessaging.sendMessage(test, msg);
				}
			}

			// TownsMessaging.sendNationMessage(nation, chatEvent.getFormat());
		} catch (NotRegisteredException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}
	}
}
