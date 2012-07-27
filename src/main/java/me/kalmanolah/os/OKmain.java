package me.kalmanolah.os;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class OKmain extends JavaPlugin {

	public static String name;
	public static String version;
	public static List<String> authors;
	public OKCommandManager commandManager = new OKCommandManager(this);
	public static Integer mode = Integer.valueOf(1);
	public static Integer maxmsgs = Integer.valueOf(30);
	public static Integer duration = Integer.valueOf(60);
	public static Integer damagepermessage = Integer.valueOf(2);
	public static String[] commands;
	public static Boolean enablemessages = Boolean.valueOf(true);
	public static HashMap<Player, Integer> players = new HashMap();
	public static HashMap<Player, Long> times = new HashMap();
	public static List<Player> punishedplayers = new ArrayList();
	public static HashMap<Player, Integer> tasks = new HashMap();
	public static List<Player> finishedtasks = new ArrayList();
	public static List<Player> manuallypunishedplayers = new ArrayList();
	public static Boolean stats;

	@Override
	public void onEnable() {
		name = getDescription().getName();
		version = getDescription().getVersion();
		authors = getDescription().getAuthors();
		OKLogger.initialize(Logger.getLogger("Minecraft"));
		OKLogger.info("Attempting to enable " + name + " v" + version + " by " + (String) authors.get(0) + "...");
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

			public void run() {
				Iterator it = OKmain.finishedtasks.iterator();
				while (it.hasNext()) {
					Player p = (Player) it.next();
					OKmain.this.getServer().getScheduler().cancelTask(((Integer) OKmain.tasks.get(p)).intValue());
					OKmain.tasks.remove(p);
					it.remove();
				}
			}
		}, 100L, 100L);
		OKConfig config = new OKConfig(this);
		config.configCheck();
		PluginManager pm = this.getServer().getPluginManager();
		new OKPlayerListener(this);
		setupCommands();
		OKLogger.info(name + " v" + version + " enabled successfully.");
	}

	@Override
	public void onDisable() {
		OKLogger.info("Attempting to disable " + name + "...");
		OKLogger.info("Terminating worker threads...");
		getServer().getScheduler().cancelTasks(this);
		OKLogger.info(name + " disabled successfully.");
	}

	public static boolean CheckPermission(Player player, String string) {
		return player.hasPermission(string);
	}

	private void setupCommands() {
		addCommand("os", new OKCmd(this));
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return this.commandManager.dispatch(sender, cmd, label, args);
	}

	private void addCommand(String command, CommandExecutor executor) {
		getCommand(command).setExecutor(executor);
		this.commandManager.addCommand(command, executor);
	}
}