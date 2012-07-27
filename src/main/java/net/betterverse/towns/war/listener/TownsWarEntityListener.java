package net.betterverse.towns.war.listener;

import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;

import net.betterverse.towns.Towns;
import net.betterverse.towns.war.TownsWar;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsWarEntityListener implements Listener {

	public TownsWarEntityListener(Towns plugin) {
		Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void entityExplode(EntityExplodeEvent event) {
		for (Block block : event.blockList()) {
			TownsWar.checkBlock(null, block, event);
		}
	}
}
