package de.bananaco.protect;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.protect.util.Meta;
import de.bananaco.protect.util.PolygonBuilder;
import de.bananaco.protect.util.ProtectedPolygon;

public class PolygonProtect extends JavaPlugin {

	Set<String> players = new HashSet<String>();
	PolygonManager poly = PolygonManager.getInstance();
	Map<String, PolygonBuilder> data = new HashMap<String, PolygonBuilder>();
	PolygonWand polyWand = new PolygonWand(this);
	PolygonBlock polyBlocks;
	PolygonPlayer polyPlayers;
	PolygonEntity polyEntitys;
	
	public void log(String input) {
		getServer().getLogger().log(Level.INFO, "[PolygonProtect] "+input);
	}
	
	@Override
	public void onDisable() {
		log("Saving loaded polygons...");
		poly.save();
		getServer().getScheduler().cancelTasks(this);
		
		log("Disable");
	}

	@Override
	public void onEnable() {
		log("Loading saved polygons...");
		poly.load();
		log("Enable");
		
		Map<String, Boolean> p = new HashMap<String, Boolean>();
		p.put("polygon.create", true);
		p.put("polygon.check", true);
		p.put("polygon.admin", true);
		p.put("polygon.edit", true);
		p.put("polygon.delete", true);
		
		Permission perm = new Permission("polygon.*", PermissionDefault.OP, p);
		getServer().getPluginManager().addPermission(perm);
		
		polyBlocks = new PolygonBlock(this.getServer().getPluginManager(), this);
		polyPlayers = new PolygonPlayer(this.getServer().getPluginManager(), this);
		polyEntitys = new PolygonEntity(this.getServer().getPluginManager(), this);
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
			public void run() {
				PolygonManager.getInstance().save();
				log("Saving polygons!");
			}
		}, 12000, 12000);
		getServer().getPluginManager().registerEvents(polyWand, this);
	}
	
	public void startBuilding(Player player) {
		players.add(player.getName());
		data.put(player.getName(), new PolygonBuilder());
	}
	
	public boolean isBuilding(Player player) {
		return players.contains(player.getName());
	}
	
	public void stopBuilding(Player player) {
		players.remove(player.getName());
		data.remove(player.getName());
	}
	/**
	 * This command is used for basic polygonal creation!
	 * ie. the /polygon command
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 * @return boolean
	 */
	public boolean onPolygonCommand(CommandSender sender, Command command, String label, String[] args) {
		// Well console can't select polygons!
		if(!(sender instanceof Player)) {
			return false;
		}
		// We can cast safely
		Player player = (Player) sender;
		if(!player.hasPermission("polygon.create")) {
			player.sendMessage(ChatColor.RED+"NO!");
			return true;
		}
		/*
		 * ARGS 1
		 */
		if(args.length == 1) {
			String start = args[0];
			// start
			if(start.equalsIgnoreCase("start")) {
			if(!players.contains(player.getName())) {
				startBuilding(player);
				player.sendMessage(ChatColor.GOLD+"You can now start selecting polygons with a gold axe!");
				return true;
				}
			else {
				player.sendMessage(ChatColor.GOLD+"You're already selecting a polygon!");
				return true;
				}
			}
			// stop
			else {
				if(players.contains(player.getName()) && !poly.contains(player.getWorld().getName(), start)) {
					data.get(player.getName()).finalise(start);
					stopBuilding(player);
					player.sendMessage(ChatColor.GOLD+"Saved as "+start);
					return true;
				}
				else {
					player.sendMessage(ChatColor.GOLD+"You haven't selected a polygon or one exists with that name!");
					return true;
				}
			}	
		}
		/*
		 * ARGS 2 
		 */
		if(args.length == 2) {
			if(args[0].equalsIgnoreCase("delete")) {
				World world = player.getWorld();
				ProtectedPolygon pl = poly.getPolygons(world.getName()).get(args[1]);
				if(pl == null) {
					sender.sendMessage("No polygon by that name!");
					return true;
				}
				poly.getPolygons(world.getName()).remove(pl.getName());
				pl.delete();
				return true;
			}
			
			if(!isBuilding(player)) {
				player.sendMessage(ChatColor.GOLD+"You're not building a polygon!");
				return true;
			}
			
			if(args[0].equalsIgnoreCase("sellable")) {
				String sellable = args[1];
				boolean sell = true;
				if(sellable.equalsIgnoreCase("false"))
					sell = false;
				data.get(player.getName()).setSellable(sell);
				player.sendMessage(ChatColor.GOLD+"Sellable set to "+sell);
				return true;
			}
			if(args[0].equalsIgnoreCase("height")) {
				int height = Integer.parseInt(args[1]);
				data.get(player.getName()).setHeight(height);
				player.sendMessage(ChatColor.GOLD+"Height set to "+height);
				return true;
			}
		}
		
		
		return false;
	}
	
	public boolean metaCheck(String arg, String[] meta) {
		for(String m : meta)
			if(arg.equalsIgnoreCase(m))
				return true;
		return false;
	}
	/**
	 * Used to set different metaData!
	 * @param sender
	 * @param command
	 * @param label
	 * @param args
	 * @return boolean
	 */
	public boolean onMetaCommand(CommandSender sender, Command command, String label, String[] args) {
		// Obviously just some security
		if(!sender.hasPermission("polygon.edit")) {
			sender.sendMessage(ChatColor.RED+"No!");
			return true;
		}
		
		if(args.length >= 2) {

		String world = getServer().getWorlds().get(0).getName();
		if(sender instanceof Player)
			world = ((Player) sender).getWorld().getName();
		String region = args[0];
		ProtectedPolygon p = poly.get(world, region);
		
		if(p == null) {
			sender.sendMessage(ChatColor.GOLD+"No polygon with that id!");
			return true;
		}
		
		if(sender instanceof Player) {
			if(!p.canFlag((Player) sender)) {
				sender.sendMessage(ChatColor.RED+"No!");
				return true;
			}
		}
		
		String target = args[1];
		if(metaCheck(target, Meta.INT)) {
			int value = Integer.parseInt(args[2]);
			for(String m : Meta.INT) {
				if(m.equalsIgnoreCase(target)) {
					p.setMeta(m, value);
					sender.sendMessage(ChatColor.GOLD+p.getName()+" had the flag "+m+" set to "+value+"!");
					return true;
				}
			}
		}
		if(metaCheck(target, Meta.LIST)) {
			if(args.length<3) {
				sender.sendMessage(ChatColor.RED+"Try again!");
				return true;
			}
			if(args.length<4 && !args[2].equalsIgnoreCase("list")) {
				sender.sendMessage(ChatColor.RED+"Try again!");
				return true;
			}
			String add = args[2];
			String value = "";
			if(args.length>3)
				value = args[3];
			if(add.equalsIgnoreCase("add")) {
				if(target.equalsIgnoreCase(Meta.OWNERS)) {
				p.getOwners().add(value);
				sender.sendMessage(ChatColor.GOLD+value+" added to "+Meta.OWNERS);
				return true;
				} else
				if(target.equalsIgnoreCase(Meta.FLAGS)) {
				p.setFlag(value, true);
				sender.sendMessage(ChatColor.GOLD+value+" set to true!");
				return true;
				}
			} else if(add.equalsIgnoreCase("remove")) {
				if(target.equalsIgnoreCase(Meta.OWNERS)) {
				p.getOwners().remove(value);
				sender.sendMessage(ChatColor.GOLD+value+" removed from "+Meta.OWNERS);
				return true;
				} else
				if(target.equalsIgnoreCase(Meta.FLAGS)) {
				p.setFlag(value, false);
				sender.sendMessage(ChatColor.GOLD+value+" set to false!");
				return true;
				}
			} else if(add.equalsIgnoreCase("list")) {
				if(target.equalsIgnoreCase(Meta.OWNERS)) {
				String data = Arrays.toString(p.getOwners().toArray(new String[p.getOwners().size()]));
				sender.sendMessage(ChatColor.GOLD+value+Meta.OWNERS+": "+data);
				return true;
				} else
				if(target.equalsIgnoreCase(Meta.FLAGS)) {
				String data = Arrays.toString(p.getFlags().toArray(new String[p.getFlags().size()]));
				sender.sendMessage(ChatColor.GOLD+value+Meta.FLAGS+": "+data);
				return true;
				}
			}
			else {
				sender.sendMessage(ChatColor.RED+"Incorrectly formatted!");
				return true;
			}
		}
		}
		return false;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		/*
		 * If the command starts with /polygon
		 */
		if(command.getName().equalsIgnoreCase("polygon")) {
			return onPolygonCommand(sender, command, label, args);
		}
		if(command.getName().equalsIgnoreCase("pmeta")) {
			return onMetaCommand(sender, command, label, args);
		}
		return true;
	}
	
}
