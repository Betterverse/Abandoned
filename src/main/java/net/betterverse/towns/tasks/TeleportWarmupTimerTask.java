package net.betterverse.towns.tasks;

import java.util.ArrayDeque;
import java.util.Queue;

import org.bukkit.Chunk;

import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsUniverse;

/**
 * @author dumptruckman
 */
public class TeleportWarmupTimerTask extends TownsTimerTask {
	private static Queue<Resident> teleportQueue;

	public TeleportWarmupTimerTask(TownsUniverse universe) {
		super(universe);
		teleportQueue = new ArrayDeque<Resident>();
	}

	@Override
	public void run() {
		long currentTime = System.currentTimeMillis();

		while (true) {
			Resident resident = teleportQueue.peek();
			if (resident == null) {
				break;
			}
			if (currentTime > resident.getTeleportRequestTime() + (TownsSettings.getTeleportWarmupTime() * 1000)) {
				resident.clearTeleportRequest();
				try {
					// Make sure the chunk we teleport to is loaded.
					Chunk chunk = resident.getTeleportDestination().getSpawn().getWorld().getChunkAt(resident.getTeleportDestination().getSpawn().getBlock());
					if (! chunk.isLoaded()) {
						chunk.load();
					}
					universe.getPlayer(resident).teleport(resident.getTeleportDestination().getSpawn());
				} catch (TownsException ignore) {
				}
				teleportQueue.poll();
			} else {
				break;
			}
		}
	}

	public static void requestTeleport(Resident resident, Town town, double cost) {
		resident.setTeleportRequestTime();
		resident.setTeleportDestination(town);
		try {
			teleportQueue.add(resident);
		} catch (NullPointerException e) {
			System.out.println("[Towns] Error: Null returned from teleport queue.");
			System.out.println(e.getStackTrace());
		}
	}

	public static void abortTeleportRequest(Resident resident) {
		if (resident != null && teleportQueue.contains(resident)) {
			teleportQueue.remove(resident);
			if ((resident.getTeleportCost() != 0) && (TownsSettings.isUsingEconomy())) {
				try {
					resident.collect(resident.getTeleportCost(), TownsSettings.getLangString("msg_cost_spawn_refund"));
					resident.setTeleportCost(0);
					TownsMessaging.sendResidentMessage(resident, TownsSettings.getLangString("msg_cost_spawn_refund"));
				} catch (TownsException e) {
					// Resident not registered exception.
				}
			}
		}
	}
}
