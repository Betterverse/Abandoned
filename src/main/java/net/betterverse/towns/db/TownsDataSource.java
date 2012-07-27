package net.betterverse.towns.db;

import java.io.IOException;
import java.util.Scanner;

import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.PlotBlockData;
import net.betterverse.towns.object.Resident;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;

//import net.betterverse.towns.TownsSettings;

/*
 * --- : Loading process : ---
 * 
 * Load all the names/keys for each world, nation, town, and resident.
 * Load each world, which loads it's town blocks.
 * Load nations, towns, and residents.
 */

/*
 * Loading Towns:
 * Make sure to load TownBlocks, then HomeBlock, then Spawn.
 */
public abstract class TownsDataSource {
	protected TownsUniverse universe;
	//protected TownsSettings settings;
	protected Towns plugin;
	protected boolean firstRun = false;

	public void initialize(Towns plugin, TownsUniverse universe) {
		this.universe = universe;
		this.plugin = plugin;
	}

	public void backup() throws IOException {
	}

	public void cleanupBackups() {
	}

	public boolean confirmContinuation(String msg) {
		Boolean choice = null;
		String input = null;
		while (choice == null) {
			System.out.println(msg);
			System.out.print("	Continue (y/n): ");
			Scanner in = new Scanner(System.in);
			input = in.next();
			input = input.toLowerCase();
			if (input.equals("y") || input.equals("yes")) {
				in.close();
				return true;
			} else if (input.equals("n") || input.equals("no")) {
				in.close();
				return false;
			}
		}
		System.out.println("[Towns] Error recieving input, exiting.");
		return false;
	}

	public void sendDebugMsg(String msg) {
		if (plugin != null) {
			TownsMessaging.sendDebugMsg(msg);
		} else {
			System.out.println("[Towns] Debug: " + msg);
		}
	}

	public boolean loadAll() {
		return loadWorldList() && loadNationList() && loadTownList() && loadResidentList() && loadWorlds() && loadNations() && loadTowns() && loadResidents() && loadRegenList() && loadTownBlocks();
	}

	public boolean saveAll() {
		return loadRegenList() && saveWorldList() && saveNationList() && saveTownList() && saveResidentList() && saveWorlds() && saveNations() && saveTowns() && saveResidents();
	}

	abstract public boolean loadResidentList();

	abstract public boolean loadTownList();

	abstract public boolean loadNationList();

	abstract public boolean loadWorldList();

	abstract public boolean loadRegenList();

	abstract public boolean loadTownBlocks();

	abstract public boolean loadResident(Resident resident);

	abstract public boolean loadTown(Town town);

	abstract public boolean loadNation(Nation nation);

	abstract public boolean loadWorld(TownsWorld world);

	abstract public boolean saveResidentList();

	abstract public boolean saveTownList();

	abstract public boolean saveNationList();

	abstract public boolean saveWorldList();

	abstract public boolean saveRegenList();

	abstract public boolean saveResident(Resident resident);

	abstract public boolean saveTown(Town town);

	abstract public boolean saveNation(Nation nation);

	abstract public boolean saveWorld(TownsWorld world);

	abstract public boolean saveTownBlock(TownBlock townBlock);

	abstract public boolean savePlotData(PlotBlockData plotChunk);

	abstract public PlotBlockData loadPlotData(String worldName, int x, int z);

	abstract public PlotBlockData loadPlotData(TownBlock townBlock);

	abstract public void deletePlotData(PlotBlockData plotChunk);

	abstract public void deleteResident(Resident resident);

	abstract public void deleteTown(Town town);

	abstract public void deleteNation(Nation nation);

	abstract public void deleteWorld(TownsWorld world);

	abstract public void deleteTownBlock(TownBlock townBlock);

	abstract public void deleteFile(String file);

	/*
	public boolean loadWorldList() {
		return loadServerWorldsList();
	}

	public boolean loadServerWorldsList() {
		sendDebugMsg("Loading Server World List");
		for (World world : plugin.getServer().getWorlds())
			try {
				//String[] split = world.getName().split("/");
				//String worldName = split[split.length-1];
				//universe.newWorld(worldName);
				universe.newWorld(world.getName());
			} catch (AlreadyRegisteredException e) {
				e.printStackTrace();
			} catch (NotRegisteredException e) {
				e.printStackTrace();
			}
		return true;
	}
	*/

	/*
	 * Load all of category
	 */

	public boolean loadResidents() {
		sendDebugMsg("Loading Residents");
		for (Resident resident : universe.getResidents()) {
			if (! loadResident(resident)) {
				System.out.println("[Towns] Loading Error: Could not read resident data '" + resident.getName() + "'.");
				return false;
			}
		}
		return true;
	}

	public boolean loadTowns() {
		sendDebugMsg("Loading Towns");
		for (Town town : universe.getTowns()) {
			if (! loadTown(town)) {
				System.out.println("[Towns] Loading Error: Could not read town data " + town.getName() + "'.");
				return false;
			}
		}
		return true;
	}

	public boolean loadNations() {
		sendDebugMsg("Loading Nations");
		for (Nation nation : universe.getNations()) {
			if (! loadNation(nation)) {
				System.out.println("[Towns] Loading Error: Could not read nation data '" + nation.getName() + "'.");
				return false;
			}
		}
		return true;
	}

	public boolean loadWorlds() {
		sendDebugMsg("Loading Worlds");
		for (TownsWorld world : universe.getWorlds()) {
			if (! loadWorld(world)) {
				System.out.println("[Towns] Loading Error: Could not read world data '" + world.getName() + "'.");
				return false;
			} else {
				// Push all Towns belonging to this world
			}
		}
		return true;
	}

	/*
	 * Save all of category
	 */

	public boolean saveResidents() {
		sendDebugMsg("Saving Residents");
		for (Resident resident : universe.getResidents()) {
			saveResident(resident);
		}
		return true;
	}

	public boolean saveTowns() {
		sendDebugMsg("Saving Towns");
		for (Town town : universe.getTowns()) {
			saveTown(town);
		}
		return true;
	}

	public boolean saveNations() {
		sendDebugMsg("Saving Nations");
		for (Nation nation : universe.getNations()) {
			saveNation(nation);
		}
		return true;
	}

	public boolean saveWorlds() {
		sendDebugMsg("Saving Worlds");
		for (TownsWorld world : universe.getWorlds()) {
			saveWorld(world);
		}
		return true;
	}
}
