package net.betterverse.worldmarket;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import net.betterverse.protect.PolygonManager;
import net.betterverse.protect.utils.ProtectedPolygon;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * WorldMarket 1.0
 * 
 * The main class for the WorldMarket plugin
 * Contains pointers to WorldMarketManager where most
 * of the workload gets passed to.
 * 
 * WorldMarketEco simply handles the economy aspect
 * 
 * @author codename_B
 */
public class WorldMarket extends JavaPlugin {

	WorldMarketManager wmm;
	WorldMarketEco eco;
	WorldMarketTick mTick;
	YamlConfiguration config = new YamlConfiguration();
	File file = new File("plugins/WorldMarket/config.yml");
	public static int hard = 10;
	
	public static void log(String input) {
		System.out.println("[WorldMarket] " + input);
	}

	public void registerPermissions() {
		try {
		if(!file.exists())
			file.createNewFile();
			config.load(file);
			hard = config.getInt("hard-limit", hard);
			config.set("hard-limit", hard);
			config.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Map<String, Boolean> oneStar = new HashMap<String, Boolean>();
		oneStar.put("worldmarket.zones."+hard, true);
		Permission twoStar = new Permission("worldmarket.zones.*", PermissionDefault.OP, oneStar);
		getServer().getPluginManager().addPermission(twoStar);
		
		for(int i=hard; i>0; i--) {
			Map<String, Boolean> oneDown = new HashMap<String, Boolean>();
			oneDown.put("worldmarket.zones."+(i-1), true);
			Permission oneUp = new Permission("worldmarket.zones."+i, PermissionDefault.FALSE, oneDown);
			getServer().getPluginManager().addPermission(oneUp);
		}
		
		
		Map<String, Boolean> user = new HashMap<String, Boolean>();
		user.put("worldmarket.message", true);
		user.put("worldmarket.rent", true);
		user.put("worldmarket.flags", true);
		user.put("worldmarket.info", true);

		Map<String, Boolean> admin = new HashMap<String, Boolean>();
		admin.put("worldmarket.user", true);
		admin.put("worldmarket.price", true);
		admin.put("worldmarket.remove", true);

		Map<String, Boolean> all = new HashMap<String, Boolean>();
		all.put("worldmarket.admin", true);
		all.put("worldmarket.zones.*", true);

		Permission userPerm = new Permission("worldmarket.user",
				PermissionDefault.FALSE, user);
		Permission adminPerm = new Permission("worldmarket.admin",
				PermissionDefault.FALSE, admin);
		Permission allPerm = new Permission("worldmarket.*",
				PermissionDefault.OP, all);

		getServer().getPluginManager().addPermission(userPerm);
		getServer().getPluginManager().addPermission(adminPerm);
		getServer().getPluginManager().addPermission(allPerm);
	}

	@Override
	public void onDisable() {
		// Disable those threads
		mTick.setRunning(false);
		// Wait for them to close
		log("Waiting for tasks to finish...");
		while (mTick.isAlive()) {

		}
		log("Disabled");
	}

	@Override
	public void onEnable() {
		// It would help if I did this
		registerPermissions();
		// Instantiate those classes
		eco = new WorldMarketEco();
		wmm = new WorldMarketManager(eco, getServer());
		mTick = new WorldMarketTick(wmm);
		// Start the threads
		mTick.start();
		// Log the enabling
		log("Enabled");
	}
	
	public String format(String input) {
		String output = ChatColor.BLUE+"[WorldMarket] "+ChatColor.WHITE+input;
		return output;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Player only!");
			return true;
		}
		Player player = (Player) sender;

		return onCommand(player, command.getName(), args);
	}

	public boolean onCommand(Player player, String command, String[] args) {
		if (args.length == 0) {
			player.sendMessage(format("Welcome to WorldMarket. Your friendly region zoning plugin."));
		}
		/*
		 * args.length == 3 This section contains "zone flag FLAG ID"
		 * Also contains /zone rent NAME renter
		 */
		if (args.length == 3) {
			if(args[0].equalsIgnoreCase("rent")) {
				String region = args[1];
				String renter = args[2];
				World world = player.getWorld();
				PolygonManager pm = wmm.getPolygonManager();
				ProtectedPolygon poly = pm.get(world.getName(), region);
				if(poly == null) {
					player.sendMessage(format("That region does not exist!"));
					return true;
				}
				if(poly.getOwners().size() > 0) {
					player.sendMessage(format("That polygon already has an owner!"));
					return true;
				}
				boolean rent = wmm.rentPolygon(renter, world, poly);
				if(rent) {
					player.sendMessage(format(region + "rented on behalf of "+renter));
					return true;
				} else {
					player.sendMessage(format("Could not rent "+region+" for "+renter+". They probably don't have enough money"));
					return true;
				}
			}
			if (args[0].equalsIgnoreCase("flags") || args[0].equalsIgnoreCase("flag")) {
				if (!player.hasPermission("worldmarket.flags")) {
					player.sendMessage(format("Waving the white flag?"));
					return true;
				}
				ProtectedPolygon region = wmm.getRegion(player);
				if (region == null) {
					player.sendMessage(format("Not in a region!"));
					return true;
				}
				boolean success = wmm.setFlag(player.getWorld(), region, args[1], args[2]);
				if(success) {
					player.sendMessage(args[1]+":"+args[2]+" set");
				} else {
					player.sendMessage(format("Could not set flags. Format?"));
				}
				return true;
			}
		}
		/*
		 * args.length == 2 This section contains "zone price AMOUNT"
		 */
		if (args.length == 2) {
			if (args[0].equalsIgnoreCase("price")) {
				if (!player.hasPermission("worldmarket.price")) {
					player.sendMessage(format("The price wasn't right!"));
					return true;
				}
				int amount = 0;
				try {
					amount = Integer.parseInt(args[1]);
				} catch (Exception e) {
					player.sendMessage(format("Incorrectly formatted number"));
					return true;
				}
				ProtectedPolygon region = wmm.getRegion(player);
				if (region == null) {
					player.sendMessage(format("Not in a region!"));
					return true;
				}
				wmm.setPrice(player.getWorld(), region, amount);
				player.sendMessage(format("Price set to " + amount));
				return true;
			}
		}
		/*
		 * args.length == 1 This section contains "zone rent" "zone remove" "zone unrent" "zone info"
		 */
		if (args.length == 1) {
			// info
			if(args[0].equalsIgnoreCase("info")) {
				if(player.hasPermission("worldmarket.info")) {
					ProtectedPolygon region = wmm.getRegion(player);
					World world = player.getWorld();
					if (region == null) {
						player.sendMessage(format("Not in a region!"));
						return true;
					}
					if(!wmm.isRentable(world, region)) {
						player.sendMessage(format("Not in a rentable region!"));
						return true;
					}
					if(wmm.isAvailable(world, region)) {
						int cost = wmm.getCost(world, region);
						player.sendMessage(format("This region is available for "+cost));
						return true;
					}
					else {
						int cost = wmm.getCost(world, region);
						String owners = "";
						if(wmm.getOwners(region) != null) {
						owners = Arrays.toString(wmm.getOwners(region).toArray(new String[wmm.getOwners(region).size()]));
						}
						player.sendMessage(format("This region is owned by "+owners));
						player.sendMessage(format("It costs them "+cost));
						return true;
					}
				} else {
					player.sendMessage(format("You don't have permission to do that!"));
					return true;
				}
			}
			// remove
			if (args[0].equalsIgnoreCase("remove")) {
				if (player.hasPermission("worldmarket.remove")) {
					ProtectedPolygon region = wmm.getRegion(player);
					if (region == null) {
						player.sendMessage(format("Not in a region!"));
						return true;
					}
					if (!wmm.isAvailable(player.getWorld(), region)) {
						wmm.clearRegion(player.getWorld(), region);
						player.sendMessage(format("Region cleared!"));
						return true;
					} else {
						player.sendMessage(format("That region has no owner/is not for rent"));
						return true;
					}
				} else {
					player.sendMessage(format("You're no admin!"));
					return true;
				}
			}
			// rent
			if (args[0].equalsIgnoreCase("rent")) {
				if (player.hasPermission("worldmarket.rent")
						&& wmm.canRent(player)) {
					ProtectedPolygon region = wmm.getRegion(player);
					if (region == null) {
						player.sendMessage(format("You are not in a region."));
						return true;
					}
					if (wmm.isAvailable(player.getWorld(), region)) {
						if (wmm.rentPolygon(player.getName(), player.getWorld(),
								region)) {
							player.sendMessage(format("Region rented!"));
							return true;
						} else {
							player.sendMessage(format("Not enough cash!"));
							return true;
						}
					} else {
						player.sendMessage(format("That region is not for rent!"));
						return true;
					}
				} else {
					player.sendMessage(format("Your credit rating didn't check out."));
					return true;
				}
			}
			// unrent
						if (args[0].equalsIgnoreCase("unrent")) {
							if (player.hasPermission("worldmarket.rent")) {
								ProtectedPolygon region = wmm.getRegion(player);
								if (region == null) {
									player.sendMessage(format("You are not in a region."));
									return true;
								}
								if (wmm.isOwner(region, player.getName())) {
									wmm.clearRegion(player.getWorld(), region);
										player.sendMessage(format("Region unrented!"));
										return true;
								} else {
									player.sendMessage(format("That region is not yours!"));
									return true;
								}
							} else {
								player.sendMessage(format("Your credit rating didn't check out."));
								return true;
							}
						}
		}
		player.sendMessage(format("Incorrectly formatted command"));
		return true;
	}

}
