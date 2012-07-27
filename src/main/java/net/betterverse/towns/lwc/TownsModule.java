package net.betterverse.towns.lwc;

import org.bukkit.entity.Player;

import com.griefcraft.lwc.LWC;
import com.griefcraft.model.Protection;
import com.griefcraft.scripting.JavaModule;
import com.griefcraft.scripting.event.LWCProtectionInteractEvent;
import com.griefcraft.sql.PhysDB;

import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.util.Colors;

public class TownsModule extends JavaModule {
	/**
	 * The towns plugin
	 */
	private Towns towns;
	public static boolean hooked = false;

	public TownsModule(Towns towns) {
		this.towns = towns;
	}

	/**
	 * Hook into LWC
	 */
	public void hook() {
		LWC.getInstance().getModuleLoader().registerModule(towns, this);
		hooked = true;
		towns.log("[Towns] Hooked into LWC!");
	}

	/**
	 * Called when a resident is removed from a town, before their plots are freed
	 * sorry for static, todo
	 *
	 * @param town
	 * @param resident
	 */
	public static void onResidentRemoved(Town town, Resident resident) {
		for (TownBlock block : resident.getTownBlocks()) {
			// we are working with a standard chunk
			int x_width = 16;
			int z_width = 16;

			// Location of the chunk
			int baseX = block.getX() * 16;
			int baseZ = block.getZ() * 16;

			PhysDB database = LWC.getInstance().getPhysicalDatabase();

			// Load the protections for the block
			for (Protection protection : database.loadProtections(resident.getName(), baseX, baseX + x_width, 0, 128, baseZ, baseZ + z_width)) {
				protection.remove();
			}
		}
	}

	@Override
	public void onProtectionInteract(LWCProtectionInteractEvent event) {
		Player player = event.getPlayer();
		Protection protection = event.getProtection();
		TownsUniverse universe = towns.getTownsUniverse();

		// Exempt lwc admins
		if (LWC.getInstance().isAdmin(player)) {
			return;
		}

		// Get the townblock
		TownBlock townBlock = universe.getTownBlock(protection.getBlock().getLocation());

		// If it's not null, check if they're a resident
		if (townBlock != null) {
			try {
				if (! townBlock.getTown().hasResident(player.getName())) {
					player.sendMessage(Colors.Red + "You may not interact with LWC protections in a town you do not reside.");
					event.setResult(Result.CANCEL);
				}
			} catch (NotRegisteredException e) {
			}
		}
	}
}
