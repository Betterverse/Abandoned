package net.betterverse.towns.object;

import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.naming.InvalidNameException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.betterverse.towns.AlreadyRegisteredException;
import net.betterverse.towns.EmptyNationException;
import net.betterverse.towns.EmptyTownException;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.TownsUtil;
import net.betterverse.towns.db.TownsDataSource;
import net.betterverse.towns.db.TownsFlatFileSource;
import net.betterverse.towns.db.TownsHModFlatFileSource;
import net.betterverse.towns.permissions.TownsPermissionSource;
import net.betterverse.towns.tasks.DailyTimerTask;
import net.betterverse.towns.tasks.HealthRegenTimerTask;
import net.betterverse.towns.tasks.MobRemovalTimerTask;
import net.betterverse.towns.tasks.ProtectionRegenTask;
import net.betterverse.towns.tasks.RepeatingTimerTask;
import net.betterverse.towns.tasks.TeleportWarmupTimerTask;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.FileMgmt;
import net.betterverse.towns.util.MinecraftTools;
import net.betterverse.towns.util.TimeMgmt;
import net.betterverse.towns.war.War;
import net.betterverse.towns.war.WarSpoils;

import static net.betterverse.towns.object.TownsObservableType.COLLECTED_NATION_TAX;
import static net.betterverse.towns.object.TownsObservableType.COLLECTED_TONW_TAX;
import static net.betterverse.towns.object.TownsObservableType.NEW_DAY;
import static net.betterverse.towns.object.TownsObservableType.NEW_NATION;
import static net.betterverse.towns.object.TownsObservableType.NEW_RESIDENT;
import static net.betterverse.towns.object.TownsObservableType.NEW_TOWN;
import static net.betterverse.towns.object.TownsObservableType.NEW_WORLD;
import static net.betterverse.towns.object.TownsObservableType.PLAYER_LOGIN;
import static net.betterverse.towns.object.TownsObservableType.PLAYER_LOGOUT;
import static net.betterverse.towns.object.TownsObservableType.REMOVE_NATION;
import static net.betterverse.towns.object.TownsObservableType.REMOVE_RESIDENT;
import static net.betterverse.towns.object.TownsObservableType.REMOVE_TOWN;
import static net.betterverse.towns.object.TownsObservableType.REMOVE_TOWN_BLOCK;
import static net.betterverse.towns.object.TownsObservableType.RENAME_NATION;
import static net.betterverse.towns.object.TownsObservableType.RENAME_TOWN;
import static net.betterverse.towns.object.TownsObservableType.TELEPORT_REQUEST;
import static net.betterverse.towns.object.TownsObservableType.TOGGLE_DAILY_TIMER;
import static net.betterverse.towns.object.TownsObservableType.TOGGLE_HEALTH_REGEN;
import static net.betterverse.towns.object.TownsObservableType.TOGGLE_MOB_REMOVAL;
import static net.betterverse.towns.object.TownsObservableType.TOGGLE_TELEPORT_WARMUP;
import static net.betterverse.towns.object.TownsObservableType.UPKEEP_NATION;
import static net.betterverse.towns.object.TownsObservableType.UPKEEP_TOWN;
import static net.betterverse.towns.object.TownsObservableType.WAR_CLEARED;
import static net.betterverse.towns.object.TownsObservableType.WAR_END;
import static net.betterverse.towns.object.TownsObservableType.WAR_SET;
import static net.betterverse.towns.object.TownsObservableType.WAR_START;

public class TownsUniverse extends TownsObject {
	public static Towns plugin;
	private HashMap<String, Resident> residents = new HashMap<String, Resident>();
	private HashMap<String, Town> towns = new HashMap<String, Town>();
	private HashMap<String, Nation> nations = new HashMap<String, Nation>();
	private static HashMap<String, TownsWorld> worlds = new HashMap<String, TownsWorld>();
	private HashMap<BlockLocation, ProtectionRegenTask> protectionRegenTasks = new HashMap<BlockLocation, ProtectionRegenTask>();
	private Set<Block> protectionPlaceholders = new HashSet<Block>();
	//private static Hashtable<String, PlotBlockData> PlotChunks = new Hashtable<String, PlotBlockData>();

	// private List<Election> elections;
	private static TownsDataSource dataSource;
	private static CachePermissions cachePermissions = new CachePermissions();
	private static TownsPermissionSource permissionSource;

	private int townsRepeatingTask = - 1;
	private int dailyTask = - 1;
	private int mobRemoveTask = - 1;
	private int healthRegenTask = - 1;
	private int teleportWarmupTask = - 1;
	private War warEvent;
	private String rootFolder;

	public TownsUniverse() {
		setName("");
		rootFolder = "";
	}

	public TownsUniverse(String rootFolder) {
		setName("");
		this.rootFolder = rootFolder;
	}

	public TownsUniverse(Towns plugin) {
		setName("");
		TownsUniverse.plugin = plugin;
	}

	public void newDay() {
		if (! isDailyTimerRunning()) {
			toggleDailyTimer(true);
		}
		//dailyTimer.schedule(new DailyTimerTask(this), 0);
		if (getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new DailyTimerTask(this)) == - 1) {
			TownsMessaging.sendErrorMsg("Could not schedule newDay.");
		}
		setChanged();
		notifyObservers(NEW_DAY);
	}

	public void toggleTownsRepeatingTimer(boolean on) {
		if (on && ! isTownsRepeatingTaskRunning()) {
			townsRepeatingTask = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new RepeatingTimerTask(this), 0, MinecraftTools.convertToTicks(TownsSettings.getPlotManagementSpeed()));
			if (townsRepeatingTask == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule Towns Timer Task.");
			}
		} else if (! on && isTownsRepeatingTaskRunning()) {
			getPlugin().getServer().getScheduler().cancelTask(townsRepeatingTask);
			townsRepeatingTask = - 1;
		}
		setChanged();
	}

	public void toggleMobRemoval(boolean on) {
		if (on && ! isMobRemovalRunning()) {
			mobRemoveTask = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new MobRemovalTimerTask(this, plugin.getServer()), 0, MinecraftTools.convertToTicks(TownsSettings.getMobRemovalSpeed()));
			if (mobRemoveTask == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule mob removal loop.");
			}
		} else if (! on && isMobRemovalRunning()) {
			getPlugin().getServer().getScheduler().cancelTask(mobRemoveTask);
			mobRemoveTask = - 1;
		}
		setChanged();
		notifyObservers(TOGGLE_MOB_REMOVAL);
	}

	public void toggleDailyTimer(boolean on) {
		if (on && ! isDailyTimerRunning()) {
			long timeTillNextDay = TownsUtil.townsTime();
			TownsMessaging.sendMsg("Time until a New Day: " + TimeMgmt.formatCountdownTime(timeTillNextDay));
			dailyTask = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new DailyTimerTask(this), MinecraftTools.convertToTicks(timeTillNextDay), MinecraftTools.convertToTicks(TownsSettings.getDayInterval()));
			if (dailyTask == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule new day loop.");
			}
		} else if (! on && isDailyTimerRunning()) {
			getPlugin().getServer().getScheduler().cancelTask(dailyTask);
			dailyTask = - 1;
		}
		setChanged();
		notifyObservers(TOGGLE_DAILY_TIMER);
	}

	public void toggleHealthRegen(boolean on) {
		if (on && ! isHealthRegenRunning()) {
			healthRegenTask = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new HealthRegenTimerTask(this, plugin.getServer()), 0, MinecraftTools.convertToTicks(TownsSettings.getHealthRegenSpeed()));
			if (healthRegenTask == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule health regen loop.");
			}
		} else if (! on && isHealthRegenRunning()) {
			getPlugin().getServer().getScheduler().cancelTask(healthRegenTask);
			healthRegenTask = - 1;
		}
		setChanged();
		notifyObservers(TOGGLE_HEALTH_REGEN);
	}

	public void toggleTeleportWarmup(boolean on) {
		if (on && ! isTeleportWarmupRunning()) {
			teleportWarmupTask = getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), new TeleportWarmupTimerTask(this), 0, 20);
			if (teleportWarmupTask == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule teleport warmup loop.");
			}
		} else if (! on && isTeleportWarmupRunning()) {
			getPlugin().getServer().getScheduler().cancelTask(teleportWarmupTask);
			teleportWarmupTask = - 1;
		}
		setChanged();
		notifyObservers(TOGGLE_TELEPORT_WARMUP);
	}

	public boolean isTownsRepeatingTaskRunning() {
		return townsRepeatingTask != - 1;
	}

	public boolean isMobRemovalRunning() {
		return mobRemoveTask != - 1;
	}

	public boolean isDailyTimerRunning() {
		return dailyTask != - 1;
	}

	public boolean isHealthRegenRunning() {
		return healthRegenTask != - 1;
	}

	public boolean isTeleportWarmupRunning() {
		return teleportWarmupTask != - 1;
	}

	public void onLogin(Player player) throws AlreadyRegisteredException, NotRegisteredException {
		Resident resident;

		// Test and kick any players with invalid names.
		if (player.getName().trim() == null) {
			player.kickPlayer("Invalid name!");
			return;
		}

		if (! hasResident(player.getName())) {
			newResident(player.getName());
			resident = getResident(player.getName());

			TownsMessaging.sendMessage(player, TownsSettings.getRegistrationMsg(player.getName()));
			resident.setRegistered(System.currentTimeMillis());
			if (! TownsSettings.getDefaultTownName().equals("")) {
				try {
					Town town = getTown(TownsSettings.getDefaultTownName());
					town.addResident(resident);
					getDataSource().saveTown(town);
				} catch (NotRegisteredException e) {
				} catch (AlreadyRegisteredException e) {
				}
			}

			getDataSource().saveResident(resident);
			getDataSource().saveResidentList();
		} else {
			resident = getResident(player.getName());
			resident.setLastOnline(System.currentTimeMillis());

			getDataSource().saveResident(resident);
		}
                
        // If the resident is in someone elses town TODO: Town always returns null *and* throws NPE?!
        Location loc = player.getLocation();
        String townName = this.getTownName(loc);
        if (townName != null) {
            Town town = this.getTown(townName);
            if (town != null) {
                if (!resident.getTown().equals(town)) {
                    double x = loc.getX(), y = loc.getY(), z = loc.getZ();
										try{
                    while (this.getTownBlock(loc).getTown() != null||(resident.getTown()!=null&&resident.getTown().equals(this.getTownBlock(loc).getTown()))) {
                        loc.add(x+1, loc.getWorld().getHighestBlockYAt(loc), z);
                    }
										}catch(NullPointerException ex) {
											//Don't do anything here, it means the player got out of the town!
										}
                    player.teleport(loc); // Kick 'em out!
                }
            }
        }

		try {
			TownsMessaging.sendTownBoard(player, resident.getTown());
		} catch (NotRegisteredException e) {
		}

		if (isWarTime()) {
			getWarEvent().sendScores(player, 3);
		}

		setChanged();
		notifyObservers(PLAYER_LOGIN);
	}

	public void onLogout(Player player) {
		try {
			Resident resident = getResident(player.getName());
			resident.setLastOnline(System.currentTimeMillis());
			getDataSource().saveResident(resident);
		} catch (NotRegisteredException e) {
		}
		setChanged();
		notifyObservers(PLAYER_LOGOUT);
	}

	/**
	 * Teleports the player to his town's spawn location. If town doesn't have a
	 * spawn or player has no town, and teleport is forced, then player is sent
	 * to the world's spawn location.
	 *
	 * @param player
	 * @param
	 */
	/*
	   public void townSpawn(Player player, boolean forceTeleport) {
			   try {
					   Resident resident = plugin.getTownsUniverse().getResident(player.getName());
					   Town town = resident.getTown();
					   player.teleport(town.getSpawn());
					   //show message if we are using Economy and are charging for spawn travel.
					   if (!plugin.isTownsAdmin(player) && TownsSettings.isUsingEconomy() && TownsSettings.getTownSpawnTravelPrice() != 0)
							   plugin.sendMsg(player, String.format(TownsSettings.getLangString("msg_cost_spawn"),
											   TownsSettings.getTownSpawnTravelPrice() + TownsEconomyObject.getEconomyCurrency()));
					   //player.teleportTo(town.getSpawn());
			   } catch (TownsException x) {
					   if (forceTeleport) {
							   player.teleport(player.getWorld().getSpawnLocation());
							   //player.teleportTo(player.getWorld().getSpawnLocation());
							   plugin.sendDebugMsg("onTownSpawn: [forced] "+player.getName());
					   } else
							   plugin.sendErrorMsg(player, x.getError());
			   }
	   }
	   */
	public Location getTownSpawnLocation(Player player) throws TownsException {
		try {
			Resident resident = plugin.getTownsUniverse().getResident(player.getName());
			Town town = resident.getTown();
			return town.getSpawn();
		} catch (TownsException x) {
			throw new TownsException("Unable to get spawn location");
		}
	}

	public void newResident(String name) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName;
		try {
			filteredName = checkAndFilterName(name);
		} catch (InvalidNameException e) {
			throw new NotRegisteredException(e.getMessage());
		}

		if (residents.containsKey(filteredName.toLowerCase())) {
			throw new AlreadyRegisteredException("A resident with the name " + filteredName + " is already in use.");
		}

		residents.put(filteredName.toLowerCase(), new Resident(filteredName));
		setChanged();
		notifyObservers(NEW_RESIDENT);
	}

	public void newTown(String name) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName;
		try {
			filteredName = checkAndFilterName(name);
		} catch (InvalidNameException e) {
			throw new NotRegisteredException(e.getMessage());
		}

		if (towns.containsKey(filteredName.toLowerCase())) {
			throw new AlreadyRegisteredException("The town " + filteredName + " is already in use.");
		}
		towns.put(filteredName.toLowerCase(), new Town(filteredName));
		setChanged();
		notifyObservers(NEW_TOWN);
	}

	/**
	 * Returns the world a town belongs to
	 *
	 * @param town
	 * @return
	 */
	public static TownsWorld getTownWorld(String townName) {
		for (TownsWorld world : worlds.values()) {
			if (world.hasTown(townName)) {
				return world;
			}
		}

		return null;
	}

	public void newNation(String name) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName;
		try {
			filteredName = checkAndFilterName(name);
		} catch (InvalidNameException e) {
			throw new NotRegisteredException(e.getMessage());
		}

		if (nations.containsKey(filteredName.toLowerCase())) {
			throw new AlreadyRegisteredException("The nation " + filteredName + " is already in use.");
		}

		nations.put(filteredName.toLowerCase(), new Nation(filteredName));
		setChanged();
		notifyObservers(NEW_NATION);
	}

	public void newWorld(String name) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName = name;
		/*
			  try {
					  filteredName = checkAndFilterName(name);
			  } catch (InvalidNameException e) {
					  throw new NotRegisteredException(e.getMessage());
			  }
			  */
		if (worlds.containsKey(filteredName.toLowerCase())) {
			throw new AlreadyRegisteredException("The world " + filteredName + " is already in use.");
		}

		worlds.put(filteredName.toLowerCase(), new TownsWorld(filteredName));
		setChanged();
		notifyObservers(NEW_WORLD);
	}

	public String checkAndFilterName(String name) throws InvalidNameException {
		String out = TownsSettings.filterName(name);

		if (! TownsSettings.isValidName(out)) {
			throw new InvalidNameException(out + " is an invalid name.");
		}

		return out;
	}

	public String[] checkAndFilterArray(String[] arr) {
		String[] out = arr;
		int count = 0;

		for (String word : arr) {
			out[count] = TownsSettings.filterName(word);
			count++;
		}

		return out;
	}

	public boolean hasResident(String name) {
		return residents.containsKey(name.toLowerCase());
	}

	public boolean hasTown(String name) {
		return towns.containsKey(name.toLowerCase());
	}

	public boolean hasNation(String name) {
		return nations.containsKey(name.toLowerCase());
	}

	public void renameTown(Town town, String newName) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName;
		try {
			filteredName = checkAndFilterName(newName);
		} catch (InvalidNameException e) {
			throw new NotRegisteredException(e.getMessage());
		}

		if (hasTown(filteredName)) {
			throw new AlreadyRegisteredException("The town " + filteredName + " is already in use.");
		}

		// TODO: Delete/rename any invites.

		List<Resident> toSave = new ArrayList<Resident>(town.getResidents());

		//Tidy up old files
		// Has to be done here else the town no longer exists and the move command may fail.
		getDataSource().deleteTown(town);

		String oldName = town.getName();
		towns.remove(oldName.toLowerCase());
		town.setName(filteredName);

		towns.put(filteredName.toLowerCase(), town);

		//Check if this is a nation capitol
		if (town.isCapital()) {
			Nation nation = town.getNation();
			nation.setCapital(town);
			getDataSource().saveNation(nation);
		}

		Town oldTown = new Town(oldName);

			town.pay(town.getHoldingBalance(), "Rename Town - Empty account of new town name.");
			oldTown.payTo(oldTown.getHoldingBalance(), town, "Rename Town - Transfer to new account");
		

		for (Resident resident : toSave) {
			getDataSource().saveResident(resident);
		}

		getDataSource().saveTown(town);
		getDataSource().saveTownList();
		getDataSource().saveWorld(town.getWorld());

		setChanged();
		notifyObservers(RENAME_TOWN);
	}

	public void renameNation(Nation nation, String newName) throws AlreadyRegisteredException, NotRegisteredException {
		String filteredName;
		try {
			filteredName = checkAndFilterName(newName);
		} catch (InvalidNameException e) {
			throw new NotRegisteredException(e.getMessage());
		}

		if (hasNation(filteredName)) {
			throw new AlreadyRegisteredException("The nation " + filteredName + " is already in use.");
		}

		// TODO: Delete/rename any invites.

		List<Town> toSave = new ArrayList<Town>(nation.getTowns());

		String oldName = nation.getName();
		nations.put(filteredName.toLowerCase(), nation);
		//Tidy up old files
		getDataSource().deleteNation(nation);

		nations.remove(oldName.toLowerCase());
		nation.setName(filteredName);
		Nation oldNation = new Nation(oldName);

				nation.pay(nation.getHoldingBalance(), "Rename Nation - Empty account of new nation name.");
				oldNation.payTo(oldNation.getHoldingBalance(), nation, "Rename Nation - Transfer to new account");
		

		for (Town town : toSave) {
			getDataSource().saveTown(town);
		}

		getDataSource().saveNation(nation);
		getDataSource().saveNationList();

		//search and update all ally/enemy lists
		List<Nation> toSaveNation = new ArrayList<Nation>(getNations());
		for (Nation toCheck : toSaveNation) {
			if (toCheck.hasAlly(oldNation) || toCheck.hasEnemy(oldNation)) {
				try {
					if (toCheck.hasAlly(oldNation)) {
						toCheck.removeAlly(oldNation);
						toCheck.addAlly(nation);
					} else {
						toCheck.removeEnemy(oldNation);
						toCheck.addEnemy(nation);
					}
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				toSave.remove(toCheck);
			}
		}

		for (Nation toCheck : toSaveNation) {
			getDataSource().saveNation(toCheck);
		}

		setChanged();
		notifyObservers(RENAME_NATION);
	}

	public Resident getResident(String name) throws NotRegisteredException {
		Resident resident = residents.get(name.toLowerCase());
		if (resident == null) {
			throw new NotRegisteredException(name + " is not registered.");
		}

		/*
			  {
					  // Attempt to load the resident and fix the files.

					  try {
							  newResident(name);
							  resident = residents.get(name.toLowerCase());
							  getDataSource().loadResident(resident);
							  //getDataSource().saveTown(resident.getTown());
							  getDataSource().saveResidentList();
					  } catch (AlreadyRegisteredException e) {
							  throw new NotRegisteredException("Failed to re-register " + name);
					  } catch (NotRegisteredException e) {
							  throw new NotRegisteredException(name + " is not registered.");
					  }

					  //
			  }
			  */
		return resident;
	}

	@Deprecated
	public void sendMessage(Player player, List<String> lines) {
		sendMessage(player, lines.toArray(new String[0]));
	}

	@Deprecated
	public void sendTownMessage(Town town, List<String> lines) {
		sendTownMessage(town, lines.toArray(new String[0]));
	}

	@Deprecated
	public void sendNationMessage(Nation nation, List<String> lines) {
		sendNationMessage(nation, lines.toArray(new String[0]));
	}

	@Deprecated
	public void sendGlobalMessage(List<String> lines) {
		sendGlobalMessage(lines.toArray(new String[0]));
	}

	@Deprecated
	public void sendGlobalMessage(String line) {
		for (Player player : getOnlinePlayers()) {
			player.sendMessage(line);
			plugin.log("[Global Message] " + player.getName() + ": " + line);
		}
	}

	@Deprecated
	public void sendMessage(Player player, String[] lines) {
		for (String line : lines) {
			player.sendMessage(line);
			//plugin.log("[send Message] " + player.getName() + ": " + line);
		}
	}

	@Deprecated
	public void sendResidentMessage(Resident resident, String[] lines) throws TownsException {
		for (String line : lines) {
			plugin.log("[Resident Msg] " + resident.getName() + ": " + line);
		}
		Player player = getPlayer(resident);
		for (String line : lines) {
			player.sendMessage(line);
		}
	}

	@Deprecated
	public void sendTownMessage(Town town, String[] lines) {
		for (String line : lines) {
			plugin.log("[Town Msg] " + town.getName() + ": " + line);
		}
		for (Player player : getOnlinePlayers(town)) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	@Deprecated
	public void sendNationMessage(Nation nation, String[] lines) {
		for (String line : lines) {
			plugin.log("[Nation Msg] " + nation.getName() + ": " + line);
		}
		for (Player player : getOnlinePlayers(nation)) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	@Deprecated
	public void sendGlobalMessage(String[] lines) {
		for (String line : lines) {
			plugin.log("[Global Msg] " + line);
		}
		for (Player player : getOnlinePlayers()) {
			for (String line : lines) {
				player.sendMessage(line);
			}
		}
	}

	@Deprecated
	public void sendResidentMessage(Resident resident, String line) throws TownsException {
		plugin.log("[Resident Msg] " + resident.getName() + ": " + line);
		Player player = getPlayer(resident);
		player.sendMessage(TownsSettings.getLangString("default_towns_prefix") + line);
	}

	@Deprecated
	public void sendTownMessage(Town town, String line) {
		plugin.log("[Town Msg] " + town.getName() + ": " + line);
		for (Player player : getOnlinePlayers(town)) {
			player.sendMessage(TownsSettings.getLangString("default_towns_prefix") + line);
		}
	}

	@Deprecated
	public void sendNationMessage(Nation nation, String line) {
		plugin.log("[Nation Msg] " + nation.getName() + ": " + line);
		for (Player player : getOnlinePlayers(nation)) {
			player.sendMessage(line);
		}
	}

	@Deprecated
	public void sendTownBoard(Player player, Town town) {
		for (String line : ChatTools.color(Colors.Gold + "[" + town.getName() + "] " + Colors.Yellow + town.getTownBoard())) {
			player.sendMessage(line);
		}
	}

	public Player getPlayer(Resident resident) throws TownsException {
		for (Player player : getOnlinePlayers()) {
			if (player.getName().equals(resident.getName())) {
				return player;
			}
		}
		throw new TownsException("Resident is not online");
	}

	public Player[] getOnlinePlayers() {
		return plugin.getServer().getOnlinePlayers();
	}

	public List<Player> getOnlinePlayers(ResidentList residents) {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : getOnlinePlayers()) {
			if (residents.hasResident(player.getName())) {
				players.add(player);
			}
		}
		return players;
	}

	public List<Player> getOnlinePlayers(Town town) {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : getOnlinePlayers()) {
			if (town.hasResident(player.getName())) {
				players.add(player);
			}
		}
		return players;
	}

	public List<Player> getOnlinePlayers(Nation nation) {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Town town : nation.getTowns()) {
			players.addAll(getOnlinePlayers(town));
		}
		return players;
	}

	/**
	 * isWilderness
	 *
	 * returns true if this block is in the wilderness
	 *
	 * @param block
	 * @return
	 */
	public boolean isWilderness(Block block) {
		WorldCoord worldCoord;

		try {
			worldCoord = new WorldCoord(getWorld(block.getWorld().getName()), Coord.parseCoord(block));
		} catch (NotRegisteredException e) {
			// No record so must be Wilderness
			return true;
		}

		try {
			return worldCoord.getTownBlock().getTown() == null;
		} catch (NotRegisteredException e) {
			// Must be wilderness
			return true;
		}
	}

	/**
	 * getTownName
	 *
	 * returns the name of the Town this location lies within
	 * if no town is registered it returns null
	 *
	 * @param loc
	 * @return
	 */
	public String getTownName(Location loc) {
		try {
			WorldCoord worldCoord = new WorldCoord(getWorld(loc.getWorld().getName()), Coord.parseCoord(loc));
			return worldCoord.getTownBlock().getTown().getName();
		} catch (NotRegisteredException e) {
			// No data so return null
			return null;
		}
	}

	/**
	 * getTownBlock
	 *
	 * returns TownBlock this location lies within
	 * if no block is registered it returns null
	 *
	 * @param loc
	 * @return
	 */
	public TownBlock getTownBlock(Location loc) {
		TownsMessaging.sendDebugMsg("Fetching TownBlock");

		try {
			WorldCoord worldCoord = new WorldCoord(getWorld(loc.getWorld().getName()), Coord.parseCoord(loc));
			return worldCoord.getTownBlock();
		} catch (NotRegisteredException e) {
			// No data so return null
			return null;
		}
	}

	public List<Resident> getResidents() {
		return new ArrayList<Resident>(residents.values());
	}

	public Set<String> getResidentKeys() {
		return residents.keySet();
	}

	public List<Town> getTowns() {
		return new ArrayList<Town>(towns.values());
	}

	public List<Nation> getNations() {
		return new ArrayList<Nation>(nations.values());
	}

	public List<TownsWorld> getWorlds() {
		return new ArrayList<TownsWorld>(worlds.values());
	}

	public List<Town> getTownsWithoutNation() {
		List<Town> townFilter = new ArrayList<Town>();
		for (Town town : getTowns()) {
			if (! town.hasNation()) {
				townFilter.add(town);
			}
		}
		return townFilter;
	}

	public List<Resident> getResidentsWithoutTown() {
		List<Resident> residentFilter = new ArrayList<Resident>();
		for (Resident resident : getResidents()) {
			if (! resident.hasTown()) {
				residentFilter.add(resident);
			}
		}
		return residentFilter;
	}

	public List<Resident> getActiveResidents() {
		List<Resident> activeResidents = new ArrayList<Resident>();
		for (Resident resident : getResidents()) {
			if (isActiveResident(resident)) {
				activeResidents.add(resident);
			}
		}
		return activeResidents;
	}

	public boolean isActiveResident(Resident resident) {
		return System.currentTimeMillis() - resident.getLastOnline() < (20 * TownsSettings.getInactiveAfter());
	}

	public List<Resident> getResidents(String[] names) {
		List<Resident> matches = new ArrayList<Resident>();
		for (String name : names) {
			try {
				matches.add(getResident(name));
			} catch (NotRegisteredException e) {
			}
		}
		return matches;
	}

	public List<Town> getTowns(String[] names) {
		List<Town> matches = new ArrayList<Town>();
		for (String name : names) {
			try {
				matches.add(getTown(name));
			} catch (NotRegisteredException e) {
			}
		}
		return matches;
	}

	public List<Nation> getNations(String[] names) {
		List<Nation> matches = new ArrayList<Nation>();
		for (String name : names) {
			try {
				matches.add(getNation(name));
			} catch (NotRegisteredException e) {
			}
		}
		return matches;
	}

	public List<String> getStatus(TownBlock townBlock) {
		return TownsFormatter.getStatus(townBlock);
	}

	public List<String> getStatus(Resident resident) {
		return TownsFormatter.getStatus(resident);
	}

	public List<String> getStatus(Town town) {
		return TownsFormatter.getStatus(town);
	}

	public List<String> getStatus(Nation nation) {
		return TownsFormatter.getStatus(nation);
	}

	public List<String> getStatus(TownsWorld world) {
		return TownsFormatter.getStatus(world);
	}

	public Town getTown(String name) throws NotRegisteredException {
		Town town = towns.get(name.toLowerCase());
		if (town == null) {
			System.out.println("[Towny] "+name + " is not registered.");
		}
		return town;
	}

	public Nation getNation(String name) throws NotRegisteredException {
		Nation nation = nations.get(name.toLowerCase());
		if (nation == null) {
			throw new NotRegisteredException(name + " is not registered.");
		}
		return nation;
	}

	public String getRootFolder() {
		if (plugin != null) {
			return plugin.getDataFolder().getPath();
		} else {
			return rootFolder;
		}
	}

	public boolean loadSettings() {
		try {
			FileMgmt.checkFolders(new String[]{getRootFolder(), getRootFolder() + FileMgmt.fileSeparator() + "settings", getRootFolder() + FileMgmt.fileSeparator() + "logs"}); // Setup the logs folder here as the logger will not yet be enabled.

			TownsSettings.loadConfig(getRootFolder() + FileMgmt.fileSeparator() + "settings" + FileMgmt.fileSeparator() + "config.yml", plugin.getVersion());
			TownsSettings.loadLanguage(getRootFolder() + FileMgmt.fileSeparator() + "settings", "english.yml");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		// Setup any defaults before we load the database.
		Coord.setCellSize(TownsSettings.getTownBlockSize());

		System.out.println("[Towns] Database: [Load] " + TownsSettings.getLoadDatabase() + " [Save] " + TownsSettings.getSaveDatabase());
		if (! loadDatabase(TownsSettings.getLoadDatabase())) {
			System.out.println("[Towns] Error: Failed to load!");
			return false;
		}

		try {
			getDataSource().cleanupBackups();
			setDataSource(TownsSettings.getSaveDatabase());
			getDataSource().initialize(plugin, this);
			try {
				getDataSource().backup();
			} catch (IOException e) {
				System.out.println("[Towns] Error: Could not create backup.");
				e.printStackTrace();
				return false;
			}

			//if (TownsSettings.isSavingOnLoad())
			//	  townsUniverse.getDataSource().saveAll();
		} catch (UnsupportedOperationException e) {
			System.out.println("[Towns] Error: Unsupported save format!");
			return false;
		}

		return true;
	}

	public boolean loadDatabase(String databaseType) {
		try {
			setDataSource(databaseType);
		} catch (UnsupportedOperationException e) {
			return false;
		}

		getDataSource().initialize(plugin, this);

		// make sure all tables are clear before loading
		worlds.clear();
		nations.clear();
		towns.clear();
		residents.clear();

		return getDataSource().loadAll();
	}

	public static TownsWorld getWorld(String name) throws NotRegisteredException {
		TownsWorld world = worlds.get(name.toLowerCase());
		/*
			if (world == null) {
					try {
							newWorld(name);
					} catch (AlreadyRegisteredException e) {
							throw new NotRegisteredException("Not registered, but already registered when trying to register.");
					} catch (NotRegisteredException e) {
							e.printStackTrace();
					}
					world = worlds.get(name.toLowerCase());
					*/
		if (world == null) {
			throw new NotRegisteredException("World not registered!");
		}
		//throw new NotRegisteredException("Could not create world " + name.toLowerCase());
		//}
		return world;
	}

	public boolean isAlly(String a, String b) {
		try {
			Resident residentA = getResident(a);
			Resident residentB = getResident(b);
			if (residentA.getTown() == residentB.getTown()) {
				return true;
			}
			if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
				return true;
			}
			if (residentA.getTown().getNation().hasAlly(residentB.getTown().getNation())) {
				return true;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return false;
	}

	public boolean isAlly(Town a, Town b) {
		try {
			if (a == b) {
				return true;
			}
			if (a.getNation() == b.getNation()) {
				return true;
			}
			if (a.getNation().hasAlly(b.getNation())) {
				return true;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return false;
	}

	public boolean canAttackEnemy(String a, String b) {
		try {
			Resident residentA = getResident(a);
			Resident residentB = getResident(b);
			if (residentA.getTown() == residentB.getTown()) {
				return false;
			}
			if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
				return false;
			}
			Nation nationA = residentA.getTown().getNation();
			Nation nationB = residentB.getTown().getNation();
			if (nationA.isNeutral() || nationB.isNeutral()) {
				return false;
			}
			if (nationA.hasEnemy(nationB)) {
				return true;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return false;
	}

	public boolean isEnemy(String a, String b) {
		try {
			Resident residentA = getResident(a);
			Resident residentB = getResident(b);
			if (residentA.getTown() == residentB.getTown()) {
				return false;
			}
			if (residentA.getTown().getNation() == residentB.getTown().getNation()) {
				return false;
			}
			if (residentA.getTown().getNation().hasEnemy(residentB.getTown().getNation())) {
				return true;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return false;
	}

	public boolean isEnemy(Town a, Town b) {
		try {
			if (a == b) {
				return false;
			}
			if (a.getNation() == b.getNation()) {
				return false;
			}
			if (a.getNation().hasEnemy(b.getNation())) {
				return true;
			}
		} catch (NotRegisteredException e) {
			return false;
		}
		return false;
	}

	public void setDataSource(String databaseType) throws UnsupportedOperationException {
		if (databaseType.equalsIgnoreCase("flatfile")) {
			setDataSource(new TownsFlatFileSource());
		} else if (databaseType.equalsIgnoreCase("flatfile-hmod")) {
			setDataSource(new TownsHModFlatFileSource());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	public void setDataSource(TownsDataSource dataSource) {
		TownsUniverse.dataSource = dataSource;
	}

	public static TownsDataSource getDataSource() {
		return dataSource;
	}

	public void setPermissionSource(TownsPermissionSource permissionSource) {
		TownsUniverse.permissionSource = permissionSource;
	}

	public static TownsPermissionSource getPermissionSource() {
		return permissionSource;
	}

	public static CachePermissions getCachePermissions() {
		return cachePermissions;
	}

	public boolean isWarTime() {
		return warEvent != null ? warEvent.isWarTime() : false;
	}

	public void collectNationTaxes() {
		for (Nation nation : new ArrayList<Nation>(nations.values())) {
			collectNationTaxes(nation);
		}
		setChanged();
		notifyObservers(COLLECTED_NATION_TAX);
	}

	public void collectNationTaxes(Nation nation){
		if (nation.getTaxes() > 0) {
			for (Town town : new ArrayList<Town>(nation.getTowns())) {
				if (town.isCapital() || ! town.hasUpkeep()) {
					continue;
				}
				if (! town.payTo(nation.getTaxes(), nation, "Nation Tax")) {
					try {
						TownsMessaging.sendNationMessage(nation, TownsSettings.getCouldntPayTaxesMsg(town, "nation"));
						nation.removeTown(town);
					} catch (EmptyNationException e) {
						// Always has 1 town (capital) so ignore
					} catch (NotRegisteredException e) {
					}
					getDataSource().saveTown(town);
					getDataSource().saveNation(nation);
				} else {
					TownsMessaging.sendTownMessage(town, TownsSettings.getPayedTownTaxMsg() + nation.getTaxes());
				}
			}
		}
	}

	public void collectTownTaxes() {
		for (Town town : new ArrayList<Town>(towns.values())) {
			collectTownTaxes(town);
		}
		setChanged();
		notifyObservers(COLLECTED_TONW_TAX);
	}

	public void collectTownTaxes(Town town) {
		//Resident Tax
		if (town.getTaxes() > 0) {
			for (Resident resident : new ArrayList<Resident>(town.getResidents())) {
				if (town.isMayor(resident) || town.hasAssistant(resident)) {
					try {
						TownsMessaging.sendResidentMessage(resident, TownsSettings.getTaxExemptMsg());
					} catch (TownsException e) {
					}
					continue;
				} else if (town.isTaxPercentage()) {
					double cost = resident.getHoldingBalance() * town.getTaxes() / 100;
					resident.payTo(cost, town, "Town Tax (Percentage)");
					try {
						TownsMessaging.sendResidentMessage(resident, TownsSettings.getPayedResidentTaxMsg() + cost);
					} catch (TownsException e) {
					}
				} else if (! resident.payTo(town.getTaxes(), town, "Town Tax")) {
					TownsMessaging.sendTownMessage(town, TownsSettings.getCouldntPayTaxesMsg(resident, "town"));
					try {
						//town.removeResident(resident);
						resident.clear();
					} catch (EmptyTownException e) {
					}
					getDataSource().saveResident(resident);
					getDataSource().saveTown(town);
				} else {
					try {
						TownsMessaging.sendResidentMessage(resident, TownsSettings.getPayedResidentTaxMsg() + town.getTaxes());
					} catch (TownsException e1) {
					}
				}
			}
		}

		//Plot Tax
		if (town.getPlotTax() > 0 || town.getCommercialPlotTax() > 0) {
			HashMap<Resident, Integer> townPlots = new HashMap<Resident, Integer>();
			HashMap<Resident, Double> townTaxes = new HashMap<Resident, Double>();
			for (TownBlock townBlock : new ArrayList<TownBlock>(town.getTownBlocks())) {
				if (! townBlock.hasResident()) {
					continue;
				}
				try {
					Resident resident = townBlock.getResident();
					if (town.isMayor(resident) || town.hasAssistant(resident)) {
						continue;
					}
					if (! resident.payTo(townBlock.getType().getTax(town), town, String.format("Plot Tax (%s)", townBlock.getType()))) {
						TownsMessaging.sendTownMessage(town, String.format(TownsSettings.getLangString("msg_couldnt_pay_plot_taxes"), resident));
						townBlock.setResident(null);
						getDataSource().saveResident(resident);
						getDataSource().saveWorld(townBlock.getWorld());
					} else {
						townPlots.put(resident, (townPlots.containsKey(resident) ? townPlots.get(resident) : 0) + 1);
						townTaxes.put(resident, (townTaxes.containsKey(resident) ? townTaxes.get(resident) : 0) + townBlock.getType().getTax(town));
					}
				} catch (NotRegisteredException e) {
				}
			}
			for (Resident resident : townPlots.keySet()) {
				try {
					int numPlots = townPlots.get(resident);
					double totalCost = townTaxes.get(resident);
					TownsMessaging.sendResidentMessage(resident, String.format(TownsSettings.getLangString("msg_payed_plot_cost"), totalCost, numPlots, town.getName()));
				} catch (TownsException e) {
				}
			}
		}
	}

	public void startWarEvent() {
		this.warEvent = new War(plugin, TownsSettings.getWarTimeWarningDelay());
		setChanged();
		notifyObservers(WAR_START);
	}

	public void endWarEvent() {
		if (isWarTime()) {
			warEvent.toggleEnd();
		}
		// Automatically makes warEvent null
		setChanged();
		notifyObservers(WAR_END);
	}

	public void clearWarEvent() {
		getWarEvent().cancelTasks(getPlugin().getServer().getScheduler());
		setWarEvent(null);
		setChanged();
		notifyObservers(WAR_CLEARED);
	}

	//TODO: throw error if null
	public War getWarEvent() {
		return warEvent;
	}

	public void setWarEvent(War warEvent) {
		this.warEvent = warEvent;
		setChanged();
		notifyObservers(WAR_SET);
	}

	public Towns getPlugin() {
		return plugin;
	}

	public void setPlugin(Towns plugin) {
		TownsUniverse.plugin = plugin;
	}

	public void removeWorld(TownsWorld world) throws UnsupportedOperationException {
		getDataSource().deleteWorld(world);
		throw new UnsupportedOperationException();
	}

	public void removeNation(Nation nation) {
		//search and remove from all ally/enemy lists
		List<Nation> toSaveNation = new ArrayList<Nation>();
		for (Nation toCheck : new ArrayList<Nation>(getNations())) {
			if (toCheck.hasAlly(nation) || toCheck.hasEnemy(nation)) {
				try {
					if (toCheck.hasAlly(nation)) {
						toCheck.removeAlly(nation);
					} else {
						toCheck.removeEnemy(nation);
					}

					toSaveNation.add(toCheck);
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for (Nation toCheck : toSaveNation) {
			getDataSource().saveNation(toCheck);
		}

		//Delete nation and save towns
		getDataSource().deleteNation(nation);
		List<Town> toSave = new ArrayList<Town>(nation.getTowns());
		nation.clear();
				nation.payTo(nation.getHoldingBalance(), new WarSpoils(), "Remove Nation");
		
		nations.remove(nation.getName().toLowerCase());
		// Clear accounts
		if (TownsSettings.isUsingEconomy()) {
			nation.setBalance(0);
		}

		plugin.updateCache();
		for (Town town : toSave) {
			getDataSource().saveTown(town);
		}
		getDataSource().saveNationList();

		setChanged();
		notifyObservers(REMOVE_NATION);
	}

	////////////////////////////////////////////

	public void removeTown(Town town) {
		removeTownBlocks(town);

		List<Resident> toSave = new ArrayList<Resident>(town.getResidents());
		TownsWorld world = town.getWorld();

		try {
			if (town.hasNation()) {
				Nation nation = town.getNation();
				nation.removeTown(town);

				getDataSource().saveNation(nation);
			}
			town.clear();
		} catch (EmptyNationException e) {
			removeNation(e.getNation());
			TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_del_nation"), e.getNation()));
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				town.payTo(town.getHoldingBalance(), new WarSpoils(), "Remove Town");
		

		for (Resident resident : toSave) {
			removeResident(resident);
			getDataSource().saveResident(resident);
		}

		towns.remove(town.getName().toLowerCase());
		// Clear accounts
		if (TownsSettings.isUsingEconomy()) {
			town.setBalance(0);
		}
		plugin.updateCache();

		getDataSource().deleteTown(town);
		getDataSource().saveTownList();
		getDataSource().saveWorld(world);

		setChanged();
		notifyObservers(REMOVE_TOWN);
	}

	public void removeResident(Resident resident) {
		Town town = null;

		if (resident.hasTown()) {
			try {
				town = resident.getTown();
			} catch (NotRegisteredException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

		//getDataSource().deleteResident(resident);
		//residents.remove(resident.getName().toLowerCase());
		try {
			if (town != null) {
				town.removeResident(resident);
				getDataSource().saveTown(town);
			}
			resident.clear();
		} catch (EmptyTownException e) {
			removeTown(town);
		} catch (NotRegisteredException e) {
			// TODO Auto-generated catch block
			// town not registered
			e.printStackTrace();
		}
		//String name = resident.getName();
		//residents.remove(name.toLowerCase());
		//plugin.deleteCache(name);
		//getDataSource().saveResidentList();
		setChanged();
		notifyObservers(REMOVE_RESIDENT);
	}

	public void removeResidentList(Resident resident) {
		String name = resident.getName();

		//search and remove from all friends lists
		List<Resident> toSave = new ArrayList<Resident>();

		for (Resident toCheck : new ArrayList<Resident>(getResidents())) {
			TownsMessaging.sendDebugMsg("Checking friends of: " + toCheck.getName());
			if (toCheck.hasFriend(resident)) {
				try {
					TownsMessaging.sendDebugMsg("	   - Removing Friend: " + resident.getName());
					toCheck.removeFriend(resident);
					toSave.add(toCheck);
				} catch (NotRegisteredException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		for (Resident toCheck : toSave) {
			getDataSource().saveResident(toCheck);
		}

		//Wipe and delete resident
		try {
			resident.clear();
		} catch (EmptyTownException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		getDataSource().deleteResident(resident);

		residents.remove(name.toLowerCase());
		// Clear accounts
		if (TownsSettings.isUsingEconomy()) {
			resident.setBalance(0);
		}
		plugin.deleteCache(name);
		getDataSource().saveResidentList();
	}

	/////////////////////////////////////////////

	public void removeTownBlock(TownBlock townBlock) {
		Resident resident = null;
		Town town = null;
		try {
			resident = townBlock.getResident();
		} catch (NotRegisteredException e) {
		}
		try {
			town = townBlock.getTown();
		} catch (NotRegisteredException e) {
		}
		TownsWorld world = townBlock.getWorld();
		world.removeTownBlock(townBlock);

		getDataSource().saveWorld(world);
		getDataSource().deleteTownBlock(townBlock);

		if (resident != null) {
			getDataSource().saveResident(resident);
		}
		if (town != null) {
			getDataSource().saveTown(town);
		}

		setChanged();
		notifyObservers(REMOVE_TOWN_BLOCK);
	}

	/**
	 * Deletes all of a specified block type from a TownBlock
	 *
	 * @param townBlock
	 * @param material
	 */
	public void deleteTownBlockMaterial(TownBlock townBlock, int material) {
		//Block block = null;
		int plotSize = TownsSettings.getTownBlockSize();

		TownsMessaging.sendDebugMsg("Processing deleteTownBlockId");

		try {
			World world = plugin.getServerWorld(townBlock.getWorld().getName());
			/*
			   if (!world.isChunkLoaded(MinecraftTools.calcChunk(townBlock.getX()), MinecraftTools.calcChunk(townBlock.getZ())))
				   return;
			   */
			int height = world.getMaxHeight() - 1;
			int worldx = townBlock.getX() * plotSize, worldz = townBlock.getZ() * plotSize;

			for (int z = 0; z < plotSize; z++) {
				for (int x = 0; x < plotSize; x++) {
					for (int y = height; y > 0; y--) { //Check from bottom up else minecraft won't remove doors
						Block block = world.getBlockAt(worldx + x, y, worldz + z);
						if (block.getTypeId() == material) {
							block.setType(Material.AIR);
						}
						block = null;
					}
				}
			}
		} catch (NotRegisteredException e1) {
			// Failed to get world.
			e1.printStackTrace();
		}
	}

	public void removeTownBlocks(Town town) {
		for (TownBlock townBlock : new ArrayList<TownBlock>(town.getTownBlocks())) {
			removeTownBlock(townBlock);
		}
	}

	public void collectTownCosts() throws TownsException {
		for (Town town : new ArrayList<Town>(towns.values())) {
			if (town.hasUpkeep()) {
				if (! town.pay(TownsSettings.getTownUpkeepCost(town), "Town Upkeep")) {
					removeTown(town);
					TownsMessaging.sendGlobalMessage(town.getName() + TownsSettings.getLangString("msg_bankrupt_town"));
				}
			}
		}

		setChanged();
		notifyObservers(UPKEEP_TOWN);
	}

	public void collectNationCosts()  {
		for (Nation nation : new ArrayList<Nation>(nations.values())) {
			if (! nation.pay(TownsSettings.getNationUpkeepCost(nation), "Nation Upkeep")) {
				removeNation(nation);
				TownsMessaging.sendGlobalMessage(nation.getName() + TownsSettings.getLangString("msg_bankrupt_nation"));
			}
			if (nation.isNeutral()) {
				if (! nation.pay(TownsSettings.getNationNeutralityCost(), "Nation Neutrality Upkeep")) {
					try {
						nation.setNeutral(false);
					} catch (TownsException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					getDataSource().saveNation(nation);
					TownsMessaging.sendNationMessage(nation, TownsSettings.getLangString("msg_nation_not_neutral"));
				}
			}
		}

		setChanged();
		notifyObservers(UPKEEP_NATION);
	}

	public List<TownBlock> getAllTownBlocks() {
		List<TownBlock> townBlocks = new ArrayList<TownBlock>();
		for (TownsWorld world : getWorlds()) {
			townBlocks.addAll(world.getTownBlocks());
		}
		return townBlocks;
	}

	public void sendUniverseTree(CommandSender sender) {
		for (String line : getTreeString(0)) {
			sender.sendMessage(line);
		}
	}

	@Override
	public List<String> getTreeString(int depth) {
		List<String> out = new ArrayList<String>();
		out.add(getTreeDepth(depth) + "Universe (" + getName() + ")");
		if (plugin != null) {
			out.add(getTreeDepth(depth + 1) + "Server (" + plugin.getServer().getName() + ")");
			out.add(getTreeDepth(depth + 2) + "Version: " + plugin.getServer().getVersion());
			out.add(getTreeDepth(depth + 2) + "Players: " + plugin.getServer().getOnlinePlayers().length + "/" + plugin.getServer().getMaxPlayers());
			out.add(getTreeDepth(depth + 2) + "Worlds (" + plugin.getServer().getWorlds().size() + "): " + Arrays.toString(plugin.getServer().getWorlds().toArray(new World[0])));
		}
		out.add(getTreeDepth(depth + 1) + "Worlds (" + getWorlds().size() + "):");
		for (TownsWorld world : getWorlds()) {
			out.addAll(world.getTreeString(depth + 2));
		}

		out.add(getTreeDepth(depth + 1) + "Nations (" + getNations().size() + "):");
		for (Nation nation : getNations()) {
			out.addAll(nation.getTreeString(depth + 2));
		}

		Collection<Town> townsWithoutNation = getTownsWithoutNation();
		out.add(getTreeDepth(depth + 1) + "Towns (" + townsWithoutNation.size() + "):");
		for (Town town : townsWithoutNation) {
			out.addAll(town.getTreeString(depth + 2));
		}

		Collection<Resident> residentsWithoutTown = getResidentsWithoutTown();
		out.add(getTreeDepth(depth + 1) + "Residents (" + residentsWithoutTown.size() + "):");
		for (Resident resident : residentsWithoutTown) {
			out.addAll(resident.getTreeString(depth + 2));
		}
		return out;
	}

	public boolean areAllAllies(List<Nation> possibleAllies) {
		if (possibleAllies.size() <= 1) {
			return true;
		} else {
			for (int i = 0; i < possibleAllies.size() - 1; i++) {
				if (! possibleAllies.get(i).hasAlly(possibleAllies.get(i + 1))) {
					return false;
				}
			}
			return true;
		}
	}

	public void sendMessageTo(ResidentList residents, String msg, String modeRequired) {
		for (Player player : getOnlinePlayers(residents)) {
			if (plugin.hasPlayerMode(player, modeRequired)) {
				player.sendMessage(msg);
			}
		}
	}

	public List<Resident> getValidatedResidents(Player player, String[] names) {
		List<Resident> invited = new ArrayList<Resident>();
		for (String name : names) {
			List<Player> matches = plugin.getServer().matchPlayer(name);
			if (matches.size() > 1) {
				String line = "Multiple players selected";
				for (Player p : matches) {
					line += ", " + p.getName();
				}
				TownsMessaging.sendErrorMsg(player, line);
			} else if (matches.size() == 1) {
				// Match found online
				try {
					Resident target = getResident(matches.get(0).getName());
					invited.add(target);
				} catch (TownsException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			} else {
				// No online matches so test for offline.
				Resident target;
				try {
					target = getResident(name);
					invited.add(target);
				} catch (NotRegisteredException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			}
		}
		return invited;
	}

	public List<Resident> getOnlineResidents(Player player, String[] names) {
		List<Resident> invited = new ArrayList<Resident>();
		for (String name : names) {
			List<Player> matches = plugin.getServer().matchPlayer(name);
			if (matches.size() > 1) {
				String line = "Multiple players selected";
				for (Player p : matches) {
					line += ", " + p.getName();
				}
				TownsMessaging.sendErrorMsg(player, line);
			} else if (matches.size() == 1) {
				try {
					Resident target = plugin.getTownsUniverse().getResident(matches.get(0).getName());
					invited.add(target);
				} catch (TownsException x) {
					TownsMessaging.sendErrorMsg(player, x.getError());
				}
			}
		}
		return invited;
	}
	/*
	private static List<Resident> getResidents(Player player, String[] names) {
		List<Resident> invited = new ArrayList<Resident>();
		for (String name : names)
			try {
				Resident target = plugin.getTownsUniverse().getResident(name);
				invited.add(target);
			} catch (TownsException x) {
				plugin.sendErrorMsg(player, x.getError());
			}
		return invited;
	}
	*/

	public List<Resident> getOnlineResidents(ResidentList residentList) {
		List<Resident> onlineResidents = new ArrayList<Resident>();
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			for (Resident resident : residentList.getResidents()) {
				if (resident.getName().equalsIgnoreCase(player.getName())) {
					onlineResidents.add(resident);
				}
			}
		}

		return onlineResidents;
	}

	public void requestTeleport(Player player, Town town, double cost) {
		try {
			TeleportWarmupTimerTask.requestTeleport(getResident(player.getName().toLowerCase()), town, cost);
		} catch (TownsException x) {
			TownsMessaging.sendErrorMsg(player, x.getError());
		}

		setChanged();
		notifyObservers(TELEPORT_REQUEST);
	}

	public void abortTeleportRequest(Resident resident) {
		TeleportWarmupTimerTask.abortTeleportRequest(resident);
	}

	public void addWarZone(WorldCoord worldCoord) {
		worldCoord.getWorld().addWarZone(worldCoord);
		plugin.updateCache(worldCoord);
	}

	public void removeWarZone(WorldCoord worldCoord) {
		worldCoord.getWorld().removeWarZone(worldCoord);
		plugin.updateCache(worldCoord);
	}

	public boolean isEnemyTownBlock(Player player, WorldCoord worldCoord) {
		try {
			return isEnemy(getResident(player.getName()).getTown(), worldCoord.getTownBlock().getTown());
		} catch (NotRegisteredException e) {
			return false;
		}
	}

	public boolean hasProtectionRegenTask(BlockLocation blockLocation) {
		for (BlockLocation location : protectionRegenTasks.keySet()) {
			if (location.isLocation(blockLocation)) {
				return true;
			}
		}
		return false;
	}

	public ProtectionRegenTask GetProtectionRegenTask(BlockLocation blockLocation) {
		for (BlockLocation location : protectionRegenTasks.keySet()) {
			if (location.isLocation(blockLocation)) {
				return protectionRegenTasks.get(location);
			}
		}
		return null;
	}

	public void addProtectionRegenTask(ProtectionRegenTask task) {
		protectionRegenTasks.put(task.getBlockLocation(), task);
	}

	public void removeProtectionRegenTask(ProtectionRegenTask task) {
		protectionRegenTasks.remove(task.getBlockLocation());
		if (protectionRegenTasks.isEmpty()) {
			protectionPlaceholders.clear();
		}
	}

	public void cancelProtectionRegenTasks() {
		for (ProtectionRegenTask task : protectionRegenTasks.values()) {
			plugin.getServer().getScheduler().cancelTask(task.getTaskId());
			task.replaceProtections();
		}
		protectionRegenTasks.clear();
		protectionPlaceholders.clear();
	}

	public boolean isPlaceholder(Block block) {
		return protectionPlaceholders.contains(block);
	}

	public void addPlaceholder(Block block) {
		protectionPlaceholders.add(block);
	}

	public void removePlaceholder(Block block) {
		protectionPlaceholders.remove(block);
	}
}
