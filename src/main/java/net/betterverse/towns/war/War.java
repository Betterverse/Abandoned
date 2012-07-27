package net.betterverse.towns.war;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import net.betterverse.towns.NotRegisteredException;
import net.betterverse.towns.Towns;
import net.betterverse.towns.TownsException;
import net.betterverse.towns.TownsFormatter;
import net.betterverse.towns.TownsMessaging;
import net.betterverse.towns.TownsSettings;
import net.betterverse.towns.object.Nation;
import net.betterverse.towns.object.Town;
import net.betterverse.towns.object.TownBlock;
import net.betterverse.towns.object.TownsEconomyObject;
import net.betterverse.towns.object.TownsUniverse;
import net.betterverse.towns.object.WorldCoord;
import net.betterverse.towns.util.ChatTools;
import net.betterverse.towns.util.Colors;
import net.betterverse.towns.util.KeyValue;
import net.betterverse.towns.util.KeyValueTable;
import net.betterverse.towns.util.MinecraftTools;
import net.betterverse.towns.util.ServerBroadCastTimerTask;
import net.betterverse.towns.util.TimeMgmt;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

//TODO: Extend a new class called TownsEvent
public class War {

	private Hashtable<WorldCoord, Integer> warZone = new Hashtable<WorldCoord, Integer>();
	private Hashtable<Town, Integer> townScores = new Hashtable<Town, Integer>();
	private List<Town> warringTowns = new ArrayList<Town>();
	private List<Nation> warringNations = new ArrayList<Nation>();
	private Towns plugin;
	private TownsUniverse universe;
	private boolean warTime = false;
	//private Timer warTimer = new Timer();
	private List<Integer> warTaskIds = new ArrayList<Integer>();
	private WarSpoils warSpoils = new WarSpoils();

	public War(Towns plugin, int startDelay) {
		this.plugin = plugin;
		this.universe = plugin.getTownsUniverse();

		setupDelay(startDelay);
	}

	/*
	 * public void setWarTimer(Timer warTimer) { this.warTimer = warTimer; }
	 *
	 * public Timer getWarTimer() { return warTimer;
	}
	 */
	public void addTaskId(int id) {
		warTaskIds.add(id);
	}

	public void clearTaskIds() {
		warTaskIds.clear();
	}

	public void cancelTasks(BukkitScheduler scheduler) {
		for (Integer id : getTaskIds()) {
			scheduler.cancelTask(id);
		}
		clearTaskIds();
	}

	public List<Integer> getTaskIds() {
		return new ArrayList<Integer>(warTaskIds);
	}

	public Towns getPlugin() {
		return plugin;
	}

	public void setPlugin(Towns plugin) {
		this.plugin = plugin;
	}

	public void setupDelay(int delay) {
		if (delay <= 0) {
			start();
		} else {
			for (Long t : TimeMgmt.getCountdownDelays(delay, TimeMgmt.defaultCountdownDelays)) {
				//Schedule the warnings leading up to the start of the war event
				//warTimer.schedule(
				//			  new ServerBroadCastTimerTask(plugin,
				//							  String.format("War starts in %s", TimeMgmt.formatCountdownTime(t))),
				//							  (delay-t)*1000);
				int id = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(getPlugin(), new ServerBroadCastTimerTask(plugin, String.format("War starts in %s", TimeMgmt.formatCountdownTime(t))), MinecraftTools.convertToTicks((delay - t) * 1000));
				if (id == - 1) {
					TownsMessaging.sendErrorMsg("Could not schedule a countdown message for war event.");
					end();
				} else {
					addTaskId(id);
				}
			}
			//warTimer.schedule(new StartWarTimerTask(universe), delay*1000);
			int id = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(getPlugin(), new StartWarTimerTask(universe), MinecraftTools.convertToTicks(delay * 1000));
			if (id == - 1) {
				TownsMessaging.sendErrorMsg("Could not schedule setup delay for war event.");
				end();
			} else {
				addTaskId(id);
			}
		}
	}

	public boolean isWarTime() {
		return warTime;
	}

	public TownsUniverse getTownsUniverse() {
		return universe;
	}

	public void start() {
		warTime = true;

		//Announce

		warSpoils.pay(TownsSettings.getBaseSpoilsOfWar(), "Start of War - Base Spoils");
		TownsMessaging.sendMsg("[War] Seeding spoils of war with " + TownsSettings.getBaseSpoilsOfWar());


		//Gather all nations at war
		for (Nation nation : universe.getNations()) {
			if (!nation.isNeutral()) {
				add(nation);
				TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_war_join_nation"), nation.getName()));
			} else if (!TownsSettings.isDeclaringNeutral()) {
				try {
					nation.setNeutral(false);
					add(nation);
					TownsMessaging.sendGlobalMessage(String.format(TownsSettings.getLangString("msg_war_join_forced"), nation.getName()));
				} catch (TownsException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		//warTimer.scheduleAtFixedRate(new WarTimerTask(this), 0, 1000);
		int id = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(getPlugin(), new WarTimerTask(this), 0, MinecraftTools.convertToTicks(1000));
		if (id == - 1) {
			TownsMessaging.sendErrorMsg("Could not schedule war event loop.");
			end();
		} else {
			addTaskId(id);
		}
		checkEnd();
	}

	public void end() {
		for (Player player : universe.getOnlinePlayers()) {
			sendStats(player);
		}
		double halfWinnings;
		// Transactions might leave 1 coin. (OH noez!)
		halfWinnings = getWarSpoils().getHoldingBalance() / 2.0;

		try {
			double nationWinnings = halfWinnings / warringNations.size(); // Again, might leave residue.
			for (Nation winningNation : warringNations) {
				getWarSpoils().payTo(nationWinnings, winningNation, "War - Nation Winnings");
				TownsMessaging.sendGlobalMessage(winningNation.getName() + " won " + nationWinnings + " " + TownsEconomyObject.getEconomyCurrency() + ".");
			}
		} catch (ArithmeticException e) {
			// A war ended with 0 nations.
		}

		try {
			KeyValue<Town, Integer> winningTownScore = getWinningTownScore();
			TownsMessaging.sendGlobalMessage(winningTownScore.key.getName() + " won " + halfWinnings + " " + TownsEconomyObject.getEconomyCurrency() + " with the score " + winningTownScore.value + ".");
		} catch (TownsException e) {
		}
	}

	public void add(Nation nation) {
		for (Town town : nation.getTowns()) {
			add(town);
		}
		warringNations.add(nation);
	}

	public void add(Town town) {
		TownsMessaging.sendTownMessage(town, TownsSettings.getJoinWarMsg(town));
		townScores.put(town, 0);
		warringTowns.add(town);
		for (TownBlock townBlock : town.getTownBlocks()) {
			if (town.isHomeBlock(townBlock)) {
				warZone.put(townBlock.getWorldCoord(), TownsSettings.getWarzoneHomeBlockHealth());
			} else {
				warZone.put(townBlock.getWorldCoord(), TownsSettings.getWarzoneTownBlockHealth());
			}
		}
	}

	public boolean isWarZone(WorldCoord worldCoord) {
		return warZone.containsKey(worldCoord);
	}

	public void townScored(Town town, int n) {
		townScores.put(town, townScores.get(town) + n);
		TownsMessaging.sendTownMessage(town, TownsSettings.getWarTimeScoreMsg(town, n));
	}

	public void damage(Town attacker, TownBlock townBlock) throws NotRegisteredException {
		WorldCoord worldCoord = townBlock.getWorldCoord();
		int hp = warZone.get(worldCoord) - 1;
		if (hp > 0) {
			warZone.put(worldCoord, hp);
			if (hp % 10 == 0) {
				universe.sendMessageTo(townBlock.getTown(), Colors.Gray + "[" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp, "wardef");
				universe.sendMessageTo(attacker, Colors.Gray + "[" + townBlock.getTown().getName() + "](" + townBlock.getCoord().toString() + ") HP: " + hp, "waratk");
			}
		} else {
			remove(attacker, townBlock);
		}
	}

	public void remove(Town attacker, TownBlock townBlock) throws NotRegisteredException {
		townScored(attacker, TownsSettings.getWarPointsForTownBlock());
		townBlock.getTown().addBonusBlocks(- 1);
		attacker.addBonusBlocks(1);
		if (!townBlock.getTown().payTo(TownsSettings.getWartimeTownBlockLossPrice(), attacker, "War - TownBlock Loss")) {
			remove(townBlock.getTown());
			TownsMessaging.sendTownMessage(townBlock.getTown(), "Your town ran out of funds to support yourself in war.");
		} else {
			TownsMessaging.sendTownMessage(townBlock.getTown(), "Your town lost " + TownsSettings.getWartimeTownBlockLossPrice() + " " + TownsEconomyObject.getEconomyCurrency() + ".");
		}
		if (townBlock.getTown().isHomeBlock(townBlock)) {
			remove(townBlock.getTown());
		} else {
			remove(townBlock.getWorldCoord());
		}
		TownsUniverse.getDataSource().saveTown(townBlock.getTown());
		TownsUniverse.getDataSource().saveTown(attacker);
	}

	public void remove(TownBlock townBlock) throws NotRegisteredException {
		if (townBlock.getTown().isHomeBlock(townBlock)) {
			remove(townBlock.getTown());
		} else {
			remove(townBlock.getWorldCoord());
		}
	}

	public void eliminate(Town town) {
		remove(town);
		try {
			checkNation(town.getNation());
		} catch (NotRegisteredException e) {
			TownsMessaging.sendErrorMsg("[War] Error checking " + town.getName() + "'s nation.");
		}
		TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeEliminatedMsg(town.getName()));
		checkEnd();
	}

	public void eliminate(Nation nation) {
		remove(nation);
		TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeEliminatedMsg(nation.getName()));
		checkEnd();
	}

	public void nationLeave(Nation nation) {
		remove(nation);
		for (Town town : nation.getTowns()) {
			remove(town);
		}
		TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeForfeitMsg(nation.getName()));
		checkEnd();
	}

	public void townLeave(Town town) {
		remove(town);
		TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeForfeitMsg(town.getName()));
		checkEnd();
	}

	public void remove(Town attacker, Nation nation) {
		townScored(attacker, TownsSettings.getWarPointsForNation());
		warringNations.remove(nation);
	}

	public void remove(Nation nation) {
		warringNations.remove(nation);
	}

	public void remove(Town attacker, Town town) throws NotRegisteredException {
		townScored(attacker, TownsSettings.getWarPointsForTown());

		for (TownBlock townBlock : town.getTownBlocks()) {
			remove(townBlock.getWorldCoord());
		}
		warringTowns.remove(town);
		try {
			if (!townsLeft(town.getNation())) {
				eliminate(town.getNation());
			}
		} catch (NotRegisteredException e) {
		}
	}

	public void remove(Town town) {
		for (TownBlock townBlock : town.getTownBlocks()) {
			remove(townBlock.getWorldCoord());
		}
		warringTowns.remove(town);
		try {
			if (!townsLeft(town.getNation())) {
				eliminate(town.getNation());
			}
		} catch (NotRegisteredException e) {
		}
	}

	public boolean townsLeft(Nation nation) {
		return warringTowns.containsAll(nation.getTowns());
	}

	public void remove(WorldCoord worldCoord) {
		try {
			Town town = worldCoord.getTownBlock().getTown();
			TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeLoseTownBlockMsg(worldCoord, town.getName()));
			warZone.remove(worldCoord);
		} catch (NotRegisteredException e) {
			TownsMessaging.sendGlobalMessage(TownsSettings.getWarTimeLoseTownBlockMsg(worldCoord));
			warZone.remove(worldCoord);
		}
	}

	public void checkEnd() {
		if (warringNations.size() <= 1) {
			toggleEnd();
		} else if (plugin.getTownsUniverse().areAllAllies(warringNations)) {
			toggleEnd();
		}
	}

	public void checkTown(Town town) {
		if (countActiveWarBlocks(town) == 0) {
			eliminate(town);
		}
	}

	public void checkNation(Nation nation) {
		if (countActiveTowns(nation) == 0) {
			eliminate(nation);
		}
	}

	public int countActiveWarBlocks(Town town) {
		int n = 0;
		for (TownBlock townBlock : town.getTownBlocks()) {
			if (warZone.containsKey(townBlock.getWorldCoord())) {
				n++;
			}
		}
		return n;
	}

	public int countActiveTowns(Nation nation) {
		int n = 0;
		for (Town town : nation.getTowns()) {
			if (warringTowns.contains(town)) {
				n++;
			}
		}
		return n;
	}

	public void toggleEnd() {
		warTime = false;
	}

	public void sendStats(Player player) {
		for (String line : getStats()) {
			player.sendMessage(line);
		}
	}

	public List<String> getStats() {
		List<String> output = new ArrayList<String>();
		output.add(ChatTools.formatTitle("War Stats"));
		output.add(Colors.Green + "  Nations: " + Colors.LightGreen + warringNations.size());
		output.add(Colors.Green + "  Towns: " + Colors.LightGreen + warringTowns.size() + " / " + townScores.size());
		output.add(Colors.Green + "  WarZone: " + Colors.LightGreen + warZone.size() + " Town blocks");
		output.add(Colors.Green + "  Spoils of War: " + Colors.LightGreen + warSpoils.getHoldingBalance() + " " + TownsEconomyObject.getEconomyCurrency());
		return output;
	}

	public void sendScores(Player player) {
		sendScores(player, 10);
	}

	public void sendScores(Player player, int maxListing) {
		for (String line : getScores(maxListing)) {
			player.sendMessage(line);
		}
	}

	/**
	 * @param maxListing Maximum lines to return. Value of -1 return all.
	 * @return A list of the current scores per town sorted in descending order.
	 */
	public List<String> getScores(int maxListing) {
		List<String> output = new ArrayList<String>();
		output.add(ChatTools.formatTitle("War - Top Scores"));
		KeyValueTable<Town, Integer> kvTable = new KeyValueTable<Town, Integer>(townScores);
		kvTable.sortByValue();
		kvTable.revese();
		int n = 0;
		for (KeyValue<Town, Integer> kv : kvTable.getKeyValues()) {
			n++;
			if (maxListing != - 1 && n > maxListing) {
				break;
			}
			Town town = (Town) kv.key;
			output.add(String.format(Colors.Blue + "%40s " + Colors.Gold + "|" + Colors.LightGray + " %4d", TownsFormatter.getFormattedName(town), (Integer) kv.value));
		}
		return output;
	}

	public boolean isWarringNation(Nation nation) {
		return warringNations.contains(nation);
	}

	public KeyValue<Town, Integer> getWinningTownScore() throws TownsException {
		KeyValueTable<Town, Integer> kvTable = new KeyValueTable<Town, Integer>(townScores);
		kvTable.sortByValue();
		kvTable.revese();
		if (kvTable.getKeyValues().size() > 0) {
			return kvTable.getKeyValues().get(0);
		} else {
			throw new TownsException();
		}
	}

	public WarSpoils getWarSpoils() {
		return warSpoils;
	}
}
