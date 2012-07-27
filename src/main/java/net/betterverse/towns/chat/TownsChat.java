package net.betterverse.towns.chat;

import java.util.logging.Logger;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import com.ensifera.animosity.craftirc.CraftIRC;

import net.betterverse.towns.Towns;
import net.betterverse.towns.chat.event.TownsPlayerHighestListener;

/**
 * TownsChat plugin to manage all Towns chat
 * Website: http://code.google.com/a/eclipselabs.org/p/towns/
 *
 * @author ElgarL
 */
public final class TownsChat {
	private TownsPlayerHighestListener TownsPlayerListener;

	private Towns towns;
	private CraftIRC craftIRC;
	private CraftIRCHandler irc;

	public TownsChat(Towns towns) {
		this.towns = towns;
		getLogger().info("-******* TownsChat enabled *******-");
		TownsPlayerListener = new TownsPlayerHighestListener(towns, irc);
		getServer().getPluginManager().registerEvents(TownsPlayerListener, towns);
		Plugin test;
		test = getServer().getPluginManager().getPlugin("CraftIRC");
		if (test != null) {
			try {
				if (Double.valueOf(test.getDescription().getVersion()) >= 3.1) {
					craftIRC = (CraftIRC) test;
					irc = new CraftIRCHandler(towns, craftIRC, "towns");
				} else {
					getLogger().warning("TownsChat requires CraftIRC version 3.1 or higher to relay chat.");
				}
			} catch (NumberFormatException e) {
				getLogger().warning("Non number format found for craftIRC version string!");
			}
		}
	}

	public Logger getLogger() {
		return getServer().getLogger();
	}

	public Towns getTowns() {
		return towns;
	}

	public Server getServer() {
		return towns.getServer();
	}
}
