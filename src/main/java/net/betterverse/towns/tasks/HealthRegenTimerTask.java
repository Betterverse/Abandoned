package net.betterverse.towns.tasks;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import net.betterverse.towns.TownsException;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownBlockType;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;

public class HealthRegenTimerTask extends TownsTimerTask {
	private Server server;

	public HealthRegenTimerTask(TownsUniverse universe, Server server) {
		super(universe);
		this.server = server;
	}

	@Override
	public void run() {
		if (universe.isWarTime()) {
			return;
		}

		for (Player player : server.getOnlinePlayers()) {
			if (player.getHealth() <= 0) {
				continue;
			}

			Coord coord = Coord.parseCoord(player);
			try {
				TownsWorld world = TownsUniverse.getWorld(player.getWorld().getName());
				TownBlock townBlock = world.getTownBlock(coord);

				if (universe.isAlly(townBlock.getTown(), universe.getResident(player.getName()).getTown())) {
					if (! townBlock.getType().equals(TownBlockType.ARENA)) // only regen if not in an arena
					{
						incHealth(player);
					}
				}
			} catch (TownsException x) {
			}
		}

		//if (TownsSettings.getDebug())
		//	System.out.println("[Towns] Debug: Health Regen");
	}

	public void incHealth(Player player) {
		int currentHP = player.getHealth();
		if (currentHP < 20) {
			player.setHealth(++ currentHP);
		}
	}
}
