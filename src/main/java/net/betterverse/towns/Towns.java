package net.betterverse.towns;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.betterverse.questioner.Questioner;
import net.betterverse.questioner.questionmanager.Option;
import net.betterverse.questioner.questionmanager.Question;
import net.betterverse.towns.chat.TownsChat;
import net.betterverse.towns.command.NationCommand;
import net.betterverse.towns.command.PlotCommand;
import net.betterverse.towns.command.ResidentCommand;
import net.betterverse.towns.command.TownCommand;
import net.betterverse.towns.command.TownsAdminCommand;
import net.betterverse.towns.command.TownsCommand;
import net.betterverse.towns.command.TownsWorldCommand;
import net.betterverse.towns.event.TownsBlockListener;
import net.betterverse.towns.event.TownsEntityListener;
import net.betterverse.towns.event.TownsEntityMonitorListener;
import net.betterverse.towns.event.TownsPlayerListener;
import net.betterverse.towns.event.TownsWorldListener;
import net.betterverse.towns.lwc.TownsModule;
import net.betterverse.towns.object.Coord;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsPermission;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.TownsWorld;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.permissions.BukkitPermSource;
import net.betterverse.towns.permissions.NullPermSource;
import net.betterverse.towns.permissions.VaultSource;
import net.betterverse.towns.questioner.TownsQuestionTask;
import net.betterverse.towns.util.FileMgmt;
import net.betterverse.towns.util.StringMgmt;
import net.betterverse.towns.war.TownsWar;
import net.betterverse.towns.war.listener.TownsWarBlockListener;
import net.betterverse.towns.war.listener.TownsWarCustomListener;
import net.betterverse.towns.war.listener.TownsWarEntityListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Towns Plugin for Bukkit
 *
 * @author Shade, ElgarL
 */
public class Towns extends JavaPlugin {

	private TownsChat chat;
	public static Economy economy = null;
	private String version = "2.0.0";
	private TownsUniverse townsUniverse;
	private Map<String, PlayerCache> playerCache = Collections.synchronizedMap(new HashMap<String, PlayerCache>());
	private Map<String, List<String>> playerMode = Collections.synchronizedMap(new HashMap<String, List<String>>());
	private Questioner questioner = new Questioner(this);
	private boolean error = false;
	// LWC <-> Towns module
	private TownsModule townsLWCModule;

	@Override
	public void onEnable() {
		System.out.println("====================	  Towns	  ========================");

		version = this.getDescription().getVersion();
		townsUniverse = new TownsUniverse(this);

		if (!townsUniverse.loadSettings()) {
			error = true;
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		setupLogger();
		TownsEconomyObject.setPlugin(this);
		checkPlugins();
		SetWorldFlags();

		//make sure the timers are stopped for a reset
		townsUniverse.toggleTownsRepeatingTimer(false);
		townsUniverse.toggleDailyTimer(false);
		townsUniverse.toggleMobRemoval(false);
		townsUniverse.toggleHealthRegen(false);
		townsUniverse.toggleTeleportWarmup(false);

		//Start timers
		townsUniverse.toggleTownsRepeatingTimer(true);
		townsUniverse.toggleDailyTimer(true);
		townsUniverse.toggleMobRemoval(true);
		townsUniverse.toggleHealthRegen(TownsSettings.hasHealthRegen());
		townsUniverse.toggleTeleportWarmup(TownsSettings.getTeleportWarmupTime() > 0);
		updateCache();

		// Setup bukkit command interfaces
		getCommand("townsadmin").setExecutor(new TownsAdminCommand(this));
		getCommand("townsworld").setExecutor(new TownsWorldCommand(this));
		getCommand("resident").setExecutor(new ResidentCommand(this));
		getCommand("towns").setExecutor(new TownsCommand(this));
		getCommand("town").setExecutor(new TownCommand(this));
		getCommand("nation").setExecutor(new NationCommand(this));
		getCommand("plot").setExecutor(new PlotCommand(this));

		TownsWar.onEnable();

		registerAllEvents();

		if (getServer().getPluginManager().getPlugin("LWC") != null) {
			townsLWCModule = new TownsModule(this);
			townsLWCModule.hook();
		} else {
			TownsLogger.log.info("[Towns] LWC not detected, LWC module disabled!");
		}

		TownsLogger.log.info("=============================================================");
		TownsLogger.log.info("[Towns] Version: " + version + " - Mod Enabled");
		TownsLogger.log.info("=============================================================");

		for (Player player : getServer().getOnlinePlayers()) {
			try {
				getTownsUniverse().onLogin(player);
			} catch (TownsException x) {
				TownsMessaging.sendErrorMsg(player, x.getError());
			}
		}

		chat = new TownsChat(this);
	}

	@Override
	public void onDisable() {
		System.out.println("==============================================================");

		if (TownsUniverse.getDataSource() != null && error == false) {
			TownsUniverse.getDataSource().saveAll();
		}

		if (error == false) {
			TownsWar.onDisable();
		}

		if (getTownsUniverse().isWarTime()) {
			getTownsUniverse().getWarEvent().toggleEnd();
		}
		townsUniverse.toggleTownsRepeatingTimer(false);
		townsUniverse.toggleDailyTimer(false);
		townsUniverse.toggleMobRemoval(false);
		townsUniverse.toggleHealthRegen(false);
		townsUniverse.toggleTeleportWarmup(false);
		townsUniverse.cancelProtectionRegenTasks();

		playerCache.clear();
		playerMode.clear();

		townsUniverse = null;

		System.out.println("[Towns] Version: " + version + " - Mod Disabled");
		System.out.println("=============================================================");

		TownsLogger.shutDown();
	}

	public Questioner getQuestioner() {
		return questioner;
	}

	public TownsChat getChat() {
		return chat;
	}

	public boolean isOnline(String playerName) {
		Player player = Bukkit.getPlayer(playerName);
		if (player != null) {
			return true;
		}
		return false;
	}

	public void registerAllEvents() {
		new TownsPlayerListener(this);
		new TownsBlockListener(this);
		new TownsEntityListener(this);
		new TownsEntityMonitorListener(this);
		new TownsWorldListener(this);
		new TownsWarBlockListener(this);
		new TownsWarCustomListener(this);
		new TownsWarEntityListener(this);
	}

	public void SetWorldFlags() {
		for (Town town : getTownsUniverse().getTowns()) {
			TownsMessaging.sendDebugMsg("[Towns] Setting flags for: " + town.getName());

			if (town.getWorld() == null) {
				TownsLogger.log.warning("[Towns Error] Detected an error with the world files. Attempting to repair");
				if (town.hasHomeBlock()) {
					try {
						TownsWorld world = town.getHomeBlock().getWorld();
						if (!world.hasTown(town)) {
							world.addTown(town);
							TownsUniverse.getDataSource().saveTown(town);
							TownsUniverse.getDataSource().saveWorld(world);
						}
					} catch (TownsException e) {
						// Error fetching homeblock
						TownsLogger.log.warning("[Towns Error] Failed get world data for: " + town.getName());
					}
				} else {
					TownsLogger.log.warning("[Towns Error] No Homeblock - Failed to detect world for: " + town.getName());
				}
			}
		}
	}

	/*
	 * private void setupDatabase() { try {
	 * getDatabase().find(Towns.class).findRowCount(); }
	 * catch(PersistenceException ex) { System.out.println("Installing database
	 * for " + getDescription().getName() + " due to first time usage");
	 * installDDL(); } }
	 */
	@Override
	public List<Class<?>> getDatabaseClasses() {
		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(Towns.class);
		return list;
	}

	private void checkPlugins() {
		List<String> using = new ArrayList<String>();
		Plugin test;

		if (TownsSettings.isUsingPermissions()) {
			test = getServer().getPluginManager().getPlugin("Vault");
			if (test != null) {
				//permissions = (PermissionsEX)test;
				getTownsUniverse().setPermissionSource(new VaultSource(this, test));
				using.add(String.format("%s v%s", "Vault", test.getDescription().getVersion()));
			} else {
				getTownsUniverse().setPermissionSource(new BukkitPermSource(this));
				using.add("BukkitPermissions");
			}
		} else {
			// Not using Permissions
			getTownsUniverse().setPermissionSource(new NullPermSource(this));
		}

		if (!(setupEconomy())) {
			TownsMessaging.sendErrorMsg("No compatible Economy plugins found. You need Vault.");

		}

		test = getServer().getPluginManager().getPlugin("Essentials");
		if (test == null) {
			TownsSettings.setUsingEssentials(false);
		} else if (TownsSettings.isUsingEssentials()) {
			using.add(String.format("%s v%s", "Essentials", test.getDescription().getVersion()));
		}

		test = getServer().getPluginManager().getPlugin("Questioner");
		if (test == null) {
			TownsSettings.setUsingQuestioner(false);
		} else if (TownsSettings.isUsingQuestioner()) {
			using.add(String.format("%s v%s", "Questioner", test.getDescription().getVersion()));
		}

		if (using.size() > 0) {
			TownsLogger.log.info("[Towns] Using: " + StringMgmt.join(using, ", "));
		}
	}

	// is permissions active
	public boolean isPermissions() {
		return TownsSettings.isUsingPermissions();
	}

	public TownsUniverse getTownsUniverse() {
		return townsUniverse;
	}

	public String getVersion() {
		return version;
	}

	public World getServerWorld(String name) throws NotRegisteredException {
		for (World world : getServer().getWorlds()) {
			if (world.getName().equals(name)) {
				return world;
			}
		}

		throw new NotRegisteredException();
	}

	public boolean hasCache(Player player) {
		return playerCache.containsKey(player.getName().toLowerCase());
	}

	public void newCache(Player player) {
		try {
			getTownsUniverse();
			playerCache.put(player.getName().toLowerCase(), new PlayerCache(TownsUniverse.getWorld(player.getWorld().getName()), player));
		} catch (NotRegisteredException e) {
			TownsMessaging.sendErrorMsg(player, "Could not create permission cache for this world (" + player.getWorld().getName() + ".");
		}
	}

	public void deleteCache(Player player) {
		deleteCache(player.getName());
	}

	public void deleteCache(String name) {
		playerCache.remove(name.toLowerCase());
	}

	public PlayerCache getCache(Player player) {
		if (!hasCache(player)) {
			newCache(player);
			try {
				getTownsUniverse();
				getCache(player).setLastTownBlock(new WorldCoord(TownsUniverse.getWorld(player.getWorld().getName()), Coord.parseCoord(player)));
			} catch (NotRegisteredException e) {
				deleteCache(player);
			}
		}

		return playerCache.get(player.getName().toLowerCase());
	}

	public void updateCache(WorldCoord worldCoord) {
		for (Player player : getServer().getOnlinePlayers()) {
			if (Coord.parseCoord(player).equals(worldCoord)) {
				getCache(player).setLastTownBlock(worldCoord); //Automatically resets permissions.
			}
		}
	}

	public void updateCache() {
		for (Player player : getServer().getOnlinePlayers()) {
			try {
				getTownsUniverse();
				getCache(player).setLastTownBlock(new WorldCoord(TownsUniverse.getWorld(player.getWorld().getName()), Coord.parseCoord(player)));
			} catch (NotRegisteredException e) {
				deleteCache(player);
			}
		}
	}

	public boolean isTownsAdmin(Player player) {
		if (player.isOp()) {
			return true;
		}
		return TownsUniverse.getPermissionSource().hasPermission(player, "towns.admin");
	}

	public void setPlayerMode(Player player, String[] modes) {
		playerMode.put(player.getName(), Arrays.asList(modes));
		TownsMessaging.sendMsg(player, ("Modes set: " + StringMgmt.join(modes, ",")));
	}

	public void removePlayerMode(Player player) {
		playerMode.remove(player.getName());
		TownsMessaging.sendMsg(player, ("Mode removed."));
	}

	public List<String> getPlayerMode(Player player) {
		return playerMode.get(player.getName());
	}

	public boolean hasPlayerMode(Player player, String mode) {
		List<String> modes = getPlayerMode(player);
		if (modes == null) {
			return false;
		} else {
			return modes.contains(mode);
		}
	}

	public List<String> getPlayerMode(String name) {
		return playerMode.get(name);
	}

	public boolean hasPlayerMode(String name, String mode) {
		List<String> modes = getPlayerMode(name);
		if (modes == null) {
			return false;
		} else {
			return modes.contains(mode);
		}
	}

	/*
	 * public boolean checkEssentialsTeleport(Player player, Location lctn) { if
	 * (!TownsSettings.isUsingEssentials() ||
	 * !TownsSettings.isAllowingTownSpawn()) return false;
	 *
	 * Plugin test = getServer().getPluginManager().getPlugin("Essentials"); if
	 * (test == null) return false; Essentials essentials = (Essentials)test;
	 * //essentials.loadClasses(); sendDebugMsg("Using Essentials");
	 *
	 * try { User user = essentials.getUser(player);
	 *
	 * if (!user.isTeleportEnabled()) return false;
	 *
	 * if (!user.isJailed()){ //user.getTeleport(); Teleport teleport =
	 * user.getTeleport(); teleport.teleport(lctn, null);
	 *
	 * }
	 * return true;
	 *
	 * } catch (Exception e) { sendErrorMsg(player, "Error: " + e.getMessage());
	 * // we still retun true here as it is a cooldown return true; }
	 *
	 * }
	 */
	public String getConfigPath() {
		return getDataFolder().getPath() + FileMgmt.fileSeparator() + "settings" + FileMgmt.fileSeparator() + "config.yml";
	}

	//public void setSetting(String root, Object value, boolean saveYML) {
	//			  TownsSettings.setProperty(root, value, saveYML);
	//}
	public Object getSetting(String root) {
		return TownsSettings.getProperty(root);
	}

	public TownsModule getTownsLWCModule() {
		return townsLWCModule;
	}

	public void log(String msg) {
		if (TownsSettings.isLogging()) {
			TownsLogger.log.info(ChatColor.stripColor(msg));
		}
	}

	public void setupLogger() {
		TownsLogger.setup(getTownsUniverse().getRootFolder(), TownsSettings.isAppendingToLog());
	}

	public boolean hasWildOverride(TownsWorld world, Player player, int blockId, TownsPermission.ActionType action) {
		//check for permissions

		if (TownsUniverse.getPermissionSource().hasPermission(player, "towns.wild." + action.toString().toLowerCase()) || TownsUniverse.getPermissionSource().hasPermission(player, "towns.wild.block." + blockId + "." + action.toString().toLowerCase())) {
			return true;
		}

		// No perms found so check world settings.
		switch (action) {
			case BUILD:
				return world.getUnclaimedZoneBuild() || world.isUnclaimedZoneIgnoreId(blockId);
			case DESTROY:
				return world.getUnclaimedZoneDestroy() || world.isUnclaimedZoneIgnoreId(blockId);
			case SWITCH:
				return world.getUnclaimedZoneSwitch() || world.isUnclaimedZoneIgnoreId(blockId);
			case ITEM_USE:
				return world.getUnclaimedZoneItemUse() || world.isUnclaimedZoneIgnoreId(blockId);
			default:
				return false;
		}
	}

	public void appendQuestion(Question question) throws Exception {
		for (Option option : question.getOptions()) {
			if (option.getReaction() instanceof TownsQuestionTask) {
				((TownsQuestionTask) option.getReaction()).setTowns(this);
			}
		}
		questioner.appendQuestion(question);
	}

	public boolean parseOnOff(String s) throws Exception {
		if (s.equalsIgnoreCase("on")) {
			return true;
		} else if (s.equalsIgnoreCase("off")) {
			return false;
		} else {
			throw new Exception(String.format(TownsSettings.getLangString("msg_err_invalid_input"), " on/off."));
		}
	}

	private boolean setupEconomy() {

		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}

		return (economy != null);
	}
}
