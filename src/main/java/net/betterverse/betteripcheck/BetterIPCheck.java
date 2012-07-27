package net.betterverse.betteripcheck;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterIPCheck extends JavaPlugin implements Listener {

	private Map<String, Set<String>> alts = new HashMap<String, Set<String>>();//they key is the ip
	private Map<String, Set<String>> ips = new HashMap<String, Set<String>>();//the key is the name

	@Override
	public void onDisable() {
		try {
			SLAPI.save(ips, this.getDataFolder() + File.separator + "ips.dat");
			SLAPI.save(alts, this.getDataFolder() + File.separator + "alts.dat");
		} catch (Exception ex) {
			Logger.getLogger(BetterIPCheck.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void onEnable() {
		getDataFolder().mkdir();
		getCommand("altcheck").setExecutor(new CExecutor(this));
		try {
			ips = (Map<String, Set<String>>) SLAPI.load(this.getDataFolder() + File.separator + "ips.dat");
			alts = (Map<String, Set<String>>) SLAPI.load(this.getDataFolder() + File.separator + "alts.dat");
		} catch (Exception ex) {
			ips = new HashMap<String, Set<String>>();
			alts = new HashMap<String, Set<String>>();
		}
		this.getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent evt) {
		String ip = evt.getPlayer().getAddress().getHostName();
		String name = evt.getPlayer().getName();
		Set<String> temp;
		if (alts.containsKey(ip)) {
			temp = alts.get(ip);
		} else {
			temp = new HashSet<String>();
		}
		temp.add(name);
		alts.put(ip, temp);

		if (ips.containsKey(name)) {
			temp = ips.get(name);
		} else {
			temp = new HashSet<String>();
		}
		temp.add(ip);
		ips.put(name, temp);
		if(alts.get(ip).size()>1) {
			sendMsg(ChatColor.DARK_RED+"The player "+ChatColor.RED+name+" has alternate accounts!");
		}
		onPlayerLogin(evt.getPlayer());
		
	}

	
	public void onPlayerLogin(Player plr) {
		Set<String> str=getAllNames(plr);
		Set<OfflinePlayer> sofl=Bukkit.getServer().getBannedPlayers();
		for(OfflinePlayer of:sofl) {
			if(str.contains(of.getName())) {
				plr.kickPlayer("One of your alts is banned, but nice try!");
			}
		}
	}
	public Set<String> getAllNames(Player plr) {
		if(alts.containsKey(plr.getAddress().getHostName()))
		return alts.get(plr.getAddress().getHostName());
		return new HashSet<String>();
	}

	public Set<String> getAllIPs(Player plr) {
		if(ips.containsKey(plr.getName()))
		return ips.get(plr.getName());
		return new HashSet<String>();
	}

	private void sendMsg(String string) {
		for(Player plr:this.getServer().getOnlinePlayers()) {
			if(plr.hasPermission("betteripcheck.staff")) plr.sendMessage(string);
		}
		System.out.println("[BetterIPCheck] "+string);
	}
}
