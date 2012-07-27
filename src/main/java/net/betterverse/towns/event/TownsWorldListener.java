package net.betterverse.towns.event;

import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class TownsWorldListener implements Listener {
    
	private final Towns plugin;

	public TownsWorldListener(Towns plugin) {
		this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void worldLoad(WorldLoadEvent event) {
		newWorld(event.getWorld().getName());
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void worldInit(WorldInitEvent event) {
		newWorld(event.getWorld().getName());
	}

	private void newWorld(String worldName) {
		//String worldName = event.getWorld().getName();
		try {
			plugin.getTownsUniverse().newWorld(worldName);
			TownsWorld world = TownsUniverse.getWorld(worldName);
			if (world == null) {
				TownsMessaging.sendErrorMsg("Could not create data for " + worldName);
			} else {
				if (! TownsUniverse.getDataSource().loadWorld(world)) {
					// First time world has been noticed
					TownsUniverse.getDataSource().saveWorld(world);
				}
			}
		} catch (AlreadyRegisteredException e) {
			// Allready loaded		
		} catch (NotRegisteredException e) {
			TownsMessaging.sendErrorMsg("Could not create data for " + worldName);
			e.printStackTrace();
		}
	}
}
