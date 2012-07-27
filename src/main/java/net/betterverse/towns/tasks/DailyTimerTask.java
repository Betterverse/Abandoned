package net.betterverse.towns.tasks;

import java.io.IOException;
import java.util.ArrayList;

import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;

public class DailyTimerTask extends TownsTimerTask {
	public DailyTimerTask(TownsUniverse universe) {
		super(universe);
	}

	@Override
	public void run() {
		long start = System.currentTimeMillis();

		TownsMessaging.sendDebugMsg("New Day");

		// Collect taxes
		if (TownsSettings.isTaxingDaily()) {
			TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_new_day_tax")));
			try {
				TownsMessaging.sendDebugMsg("Collecting Town Taxes");
				universe.collectTownTaxes();
				TownsMessaging.sendDebugMsg("Collecting Nation Taxes");
				universe.collectNationTaxes();
				TownsMessaging.sendDebugMsg("Collecting Town Costs");
				universe.collectTownCosts();
				TownsMessaging.sendDebugMsg("Collecting Nation Costs");
				universe.collectNationCosts();
			} catch (TownsException e) {
				// TODO king exception
				e.printStackTrace();
			}
		} else {
			TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_new_day")));
		}

		// Automatically delete old residents
		if (TownsSettings.isDeletingOldResidents()) {
			TownsMessaging.sendDebugMsg("Scanning for old residents...");
			for (Resident resident : new ArrayList<Resident>(universe.getResidents())) {
				if (! resident.isNPC() && (System.currentTimeMillis() - resident.getLastOnline() > (TownsSettings.getDeleteTime() * 1000)) && ! plugin.isOnline(resident.getName())) {
					TownsMessaging.sendMsg("Deleting resident: " + resident.getName());
					universe.removeResident(resident);
					universe.removeResidentList(resident);
				}
			}
		}

		// Backups
		TownsMessaging.sendDebugMsg("Cleaning up old backups.");
		TownsUniverse.getDataSource().cleanupBackups();
		if (TownsSettings.isBackingUpDaily()) {
			try {
				TownsMessaging.sendDebugMsg("Making backup.");
				TownsUniverse.getDataSource().backup();
			} catch (IOException e) {
				TownsMessaging.sendErrorMsg("Could not create backup.");
				System.out.print(e.getStackTrace());
			}
		}

		TownsMessaging.sendDebugMsg("Finished New Day Code");
		TownsMessaging.sendDebugMsg("Universe Stats:");
		TownsMessaging.sendDebugMsg("	Residents: " + universe.getResidents().size());
		TownsMessaging.sendDebugMsg("	Towns: " + universe.getTowns().size());
		TownsMessaging.sendDebugMsg("	Nations: " + universe.getNations().size());
		for (TownsWorld world : universe.getWorlds()) {
			TownsMessaging.sendDebugMsg("	" + world.getName() + " (townblocks): " + world.getTownBlocks().size());
		}

		TownsMessaging.sendDebugMsg("Memory (Java Heap):");
		TownsMessaging.sendDebugMsg(String.format("%8d Mb (max)", Runtime.getRuntime().maxMemory() / 1024 / 1024));
		TownsMessaging.sendDebugMsg(String.format("%8d Mb (total)", Runtime.getRuntime().totalMemory() / 1024 / 1024));
		TownsMessaging.sendDebugMsg(String.format("%8d Mb (free)", Runtime.getRuntime().freeMemory() / 1024 / 1024));
		TownsMessaging.sendDebugMsg(String.format("%8d Mb (used=total-free)", (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024));
		TownsMessaging.sendDebugMsg("newDay took " + (System.currentTimeMillis() - start) + "ms");
	}
}
