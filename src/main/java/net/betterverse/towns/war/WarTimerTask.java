package net.betterverse.towns.war;

import org.bukkit.entity.Player;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.tasks.TownsTimerTask;

public class WarTimerTask extends TownsTimerTask {
	War warEvent;

	public WarTimerTask(War warEvent) {
		super(warEvent.getTownsUniverse());
		this.warEvent = warEvent;
	}

	@Override
	public void run() {
		//TODO: check if war has ended and end gracefully
		if (! warEvent.isWarTime()) {
			warEvent.end();
			universe.clearWarEvent();
			universe.getPlugin().updateCache();
			TownsMessaging.sendDebugMsg("War ended.");
			return;
		}

		int numPlayers = 0;
		for (Player player : universe.getOnlinePlayers()) {
			numPlayers += 1;
			TownsMessaging.sendDebugMsg("[War] " + player.getName() + ": ");
			try {
				Resident resident = universe.getResident(player.getName());
				if (resident.hasNation()) {
					Nation nation = resident.getTown().getNation();
					TownsMessaging.sendDebugMsg("[War]   hasNation");
					if (nation.isNeutral()) {
						if (warEvent.isWarringNation(nation)) {
							warEvent.nationLeave(nation);
						}
						continue;
					}
					TownsMessaging.sendDebugMsg("[War]   notNeutral");
					if (! warEvent.isWarringNation(nation)) {
						continue;
					}
					TownsMessaging.sendDebugMsg("[War]   warringNation");
					//TODO: Cache player coord & townblock

					WorldCoord worldCoord = new WorldCoord(TownsUniverse.getWorld(player.getWorld().getName()), Coord.parseCoord(player));
					if (! warEvent.isWarZone(worldCoord)) {
						continue;
					}
					TownsMessaging.sendDebugMsg("[War]   warZone");
					if (player.getLocation().getBlockY() < TownsSettings.getMinWarHeight()) {
						continue;
					}
					TownsMessaging.sendDebugMsg("[War]   aboveMinHeight");
					TownBlock townBlock = worldCoord.getTownBlock(); //universe.getWorld(player.getWorld().getName()).getTownBlock(worldCoord);
					if (nation == townBlock.getTown().getNation() || townBlock.getTown().getNation().hasAlly(nation)) {
						continue;
					}
					TownsMessaging.sendDebugMsg("[War]   notAlly");
					//Enemy nation
					warEvent.damage(resident.getTown(), townBlock);
					TownsMessaging.sendDebugMsg("[War]   damaged");
				}
			} catch (NotRegisteredException e) {
				continue;
			}
		}

		TownsMessaging.sendDebugMsg("[War] # Players: " + numPlayers);
	}
}
